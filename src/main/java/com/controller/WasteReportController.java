package com.controller;

import com.dto.request.WasteReportRequest;
import com.entity.*;
import com.repository.ComplaintRepository;
import com.repository.WasteTypeRepository;
import com.service.WasteReportService;
import com.util.FileUploadUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/citizen")
public class WasteReportController {

    private final WasteReportService wasteReportService;
    private final WasteTypeRepository wasteTypeRepository;
    private final ComplaintRepository complaintRepository;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public WasteReportController(WasteReportService wasteReportService,
                                  WasteTypeRepository wasteTypeRepository,
                                  ComplaintRepository complaintRepository) {
        this.wasteReportService = wasteReportService;
        this.wasteTypeRepository = wasteTypeRepository;
        this.complaintRepository = complaintRepository;
    }

    @GetMapping("/dashboard")
    public String citizenDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        List<WasteReport> myReports = wasteReportService.getReportsByCitizen(currentUser.getId());
        List<Complaint> myComplaints = complaintRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        model.addAttribute("reports", myReports);
        model.addAttribute("complaints", myComplaints);
        model.addAttribute("user", currentUser);
        model.addAttribute("totalReports", myReports.size());
        model.addAttribute("pendingCount",
                myReports.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        model.addAttribute("collectedCount",
                myReports.stream().filter(r -> "COLLECTED".equals(r.getStatus())).count());
        return "citizen/dashboard";
    }

    @GetMapping("/report-form")
    public String reportForm(Model model) {
        List<WasteType> wasteTypes = wasteTypeRepository.findByIsActiveTrue();
        model.addAttribute("wasteTypes", wasteTypes);
        model.addAttribute("wasteReportRequest", new WasteReportRequest());
        return "citizen/report-form";
    }

    @PostMapping("/report-form")
    public String submitReport(@Valid @ModelAttribute WasteReportRequest request,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("wasteTypes", wasteTypeRepository.findByIsActiveTrue());
            return "citizen/report-form";
        }

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = "/uploads/" + FileUploadUtil.saveFile(imageFile, uploadDir);
            } catch (IOException e) {
                model.addAttribute("errorMsg", "Lỗi upload ảnh!");
                model.addAttribute("wasteTypes", wasteTypeRepository.findByIsActiveTrue());
                return "citizen/report-form";
            }
        }

        wasteReportService.createReport(request, currentUser, imageUrl);
        redirectAttributes.addFlashAttribute("successMsg", "Báo cáo đã được gửi thành công!");
        return "redirect:/citizen/dashboard";
    }

    // ===== COMPLAINT =====

    @PostMapping("/complaints/submit")
    public String submitComplaint(@RequestParam Long reportId,
                                   @RequestParam String subject,
                                   @RequestParam String content,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        WasteReport report = wasteReportService.findById(reportId).orElse(null);
        if (report == null) {
            ra.addFlashAttribute("errorMsg", "Báo cáo không tồn tại!");
            return "redirect:/citizen/dashboard";
        }

        // Chỉ cho phép khiếu nại khi đã COLLECTED
        if (!"COLLECTED".equals(report.getStatus())) {
            ra.addFlashAttribute("errorMsg", "Chỉ có thể khiếu nại sau khi rác đã được thu gom!");
            return "redirect:/citizen/dashboard";
        }

        Complaint complaint = new Complaint();
        complaint.setUser(currentUser);
        complaint.setWasteReport(report);
        complaint.setSubject(subject);
        complaint.setContent(content);
        complaint.setStatus("OPEN");
        complaint.setCreatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        ra.addFlashAttribute("successMsg", "Khiếu nại đã được gửi thành công! Chúng tôi sẽ xem xét sớm nhất.");
        return "redirect:/citizen/dashboard";
    }
}