package com.controller;

import com.entity.*;
import com.repository.*;
import com.service.WasteReportService;
import com.util.FileUploadUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collector")
public class CollectorController {

    private final CollectorRepository collectorRepository;
    private final AssignmentRepository assignmentRepository;
    private final WasteReportService wasteReportService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public CollectorController(CollectorRepository collectorRepository,
                                AssignmentRepository assignmentRepository,
                                WasteReportService wasteReportService) {
        this.collectorRepository = collectorRepository;
        this.assignmentRepository = assignmentRepository;
        this.wasteReportService = wasteReportService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        Collector collector = collectorRepository.findByUserId(currentUser.getId()).orElse(null);
        model.addAttribute("user", currentUser);
        model.addAttribute("collector", collector);

        if (collector != null) {
            List<Assignment> allAssignments = assignmentRepository.findByCollectorId(collector.getId());

            List<Assignment> activeAssignments = allAssignments.stream()
                    .filter(a -> !a.getStatus().equals("COMPLETED") && !a.getStatus().equals("FAILED"))
                    .collect(Collectors.toList());

            List<Assignment> completedAssignments = allAssignments.stream()
                    .filter(a -> a.getStatus().equals("COMPLETED"))
                    .collect(Collectors.toList());

            model.addAttribute("assignments", allAssignments);
            model.addAttribute("activeAssignments", activeAssignments);
            model.addAttribute("completedAssignments", completedAssignments);
            model.addAttribute("totalCompleted", completedAssignments.size());
            model.addAttribute("totalActive", activeAssignments.size());
        }

        return "collector/dashboard";
    }

    // ✅ ENDPOINT MỚI: Toggle trạng thái sẵn sàng / bận
    @PostMapping("/toggle-status")
    public String toggleStatus(HttpSession session, RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        collectorRepository.findByUserId(currentUser.getId()).ifPresent(collector -> {
            Boolean current = collector.getIsAvailable();
            collector.setIsAvailable(current == null || !current);
            collectorRepository.save(collector);
        });

        ra.addFlashAttribute("successMsg", "Đã cập nhật trạng thái!");
        return "redirect:/collector/dashboard";
    }

    @PostMapping("/assignments/{assignmentId}/update")
    public String updateAssignment(@PathVariable Long assignmentId,
                                    @RequestParam String status,
                                    @RequestParam(value = "completionImage", required = false) MultipartFile completionImage,
                                    @RequestParam(value = "notes", required = false) String notes,
                                    RedirectAttributes ra) {
        assignmentRepository.findById(assignmentId).ifPresent(assignment -> {
            assignment.setStatus(status);

            if (notes != null && !notes.isEmpty()) {
                assignment.setNotes(notes);
            }

            if (completionImage != null && !completionImage.isEmpty()) {
                try {
                    String imageUrl = "/uploads/" + FileUploadUtil.saveFile(completionImage, uploadDir);
                    assignment.setNotes((notes != null ? notes + " | " : "") + "Ảnh: " + imageUrl);
                } catch (IOException e) {
                    System.err.println("Lỗi upload ảnh: " + e.getMessage());
                }
            }

            if ("COMPLETED".equals(status)) {
                assignment.setCompletedAt(LocalDateTime.now());
                wasteReportService.updateStatus(assignment.getWasteReport().getId(), "COLLECTED");
            } else if ("ON_THE_WAY".equals(status)) {
                wasteReportService.updateStatus(assignment.getWasteReport().getId(), "ON_THE_WAY");
            }

            assignmentRepository.save(assignment);
        });

        ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công!");
        return "redirect:/collector/dashboard";
    }
}