package com.controller;

import com.entity.*;
import com.repository.*;
import com.service.WasteReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/enterprise")
public class EnterpriseController {

    private final WasteReportService wasteReportService;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorRepository collectorRepository;
    private final AssignmentRepository assignmentRepository;
    private final ComplaintRepository complaintRepository;
    private final WasteTypeRepository wasteTypeRepository;

    public EnterpriseController(WasteReportService wasteReportService,
                                 EnterpriseRepository enterpriseRepository,
                                 CollectorRepository collectorRepository,
                                 AssignmentRepository assignmentRepository,
                                 ComplaintRepository complaintRepository,
                                 WasteTypeRepository wasteTypeRepository) {
        this.wasteReportService = wasteReportService;
        this.enterpriseRepository = enterpriseRepository;
        this.collectorRepository = collectorRepository;
        this.assignmentRepository = assignmentRepository;
        this.complaintRepository = complaintRepository;
        this.wasteTypeRepository = wasteTypeRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        Enterprise enterprise = enterpriseRepository.findByUserId(currentUser.getId()).orElse(null);
        List<WasteReport> pendingReports = wasteReportService.getReportsByStatus("PENDING");
        List<WasteReport> acceptedReports = wasteReportService.getReportsByStatus("ACCEPTED");
        List<Complaint> allComplaints = complaintRepository.findAllByOrderByCreatedAtDesc();
        List<Complaint> openComplaints = allComplaints.stream()
                .filter(c -> "OPEN".equals(c.getStatus()) || "IN_REVIEW".equals(c.getStatus()))
                .toList();

        // Thống kê
        List<WasteReport> allReports = wasteReportService.getAllReports();
        long totalReports  = allReports.size();
        long pendingCount  = allReports.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long assignedCount = allReports.stream().filter(r -> "ASSIGNED".equals(r.getStatus())).count();
        long onTheWayCount = allReports.stream().filter(r -> "ON_THE_WAY".equals(r.getStatus())).count();
        long collectedCount= allReports.stream().filter(r -> "COLLECTED".equals(r.getStatus())).count();
        long rejectedCount = allReports.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();

        // Thống kê khối lượng
        double totalKg = allReports.stream()
                .filter(r -> "COLLECTED".equals(r.getStatus()) && r.getQuantityKg() != null)
                .mapToDouble(WasteReport::getQuantityKg).sum();

        java.util.Map<String, Double> weightByType = new java.util.LinkedHashMap<>();
        allReports.stream()
                .filter(r -> "COLLECTED".equals(r.getStatus()) && r.getQuantityKg() != null)
                .forEach(r -> weightByType.merge(r.getWasteType().getName(), r.getQuantityKg(), Double::sum));

        java.util.Map<String, Double> weightByMonth = new java.util.LinkedHashMap<>();
        allReports.stream()
                .filter(r -> "COLLECTED".equals(r.getStatus()) && r.getQuantityKg() != null && r.getCreatedAt() != null)
                .forEach(r -> {
                    String month = r.getCreatedAt().getMonthValue() + "/" + r.getCreatedAt().getYear();
                    weightByMonth.merge(month, r.getQuantityKg(), Double::sum);
                });

        List<Assignment> activeAssignments = assignmentRepository.findAll().stream()
                .filter(a -> !"COMPLETED".equals(a.getStatus()) && !"FAILED".equals(a.getStatus()))
                .toList();

        // Waste types để hiển thị trong form năng lực
        List<WasteType> allWasteTypes = wasteTypeRepository.findByIsActiveTrue();

        model.addAttribute("enterprise", enterprise);
        model.addAttribute("pendingReports", pendingReports);
        model.addAttribute("acceptedReports", acceptedReports);
        model.addAttribute("user", currentUser);
        model.addAttribute("allComplaints", allComplaints);
        model.addAttribute("openComplaints", openComplaints);
        model.addAttribute("activeAssignments", activeAssignments);
        model.addAttribute("totalReports", totalReports);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("onTheWayCount", onTheWayCount);
        model.addAttribute("collectedCount", collectedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalKg", totalKg);
        model.addAttribute("weightByType", weightByType);
        model.addAttribute("weightByMonth", weightByMonth);
        model.addAttribute("allWasteTypes", allWasteTypes);
        model.addAttribute("collectors", collectorRepository.findAll());

        return "enterprise/dashboard";
    }

    // ===== CẬP NHẬT NĂNG LỰC =====
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String companyName,
                                 @RequestParam(required = false) String licenseNumber,
                                 @RequestParam(required = false) Double processingCapacity,
                                 @RequestParam(required = false) String serviceArea,
                                 @RequestParam(required = false) List<String> acceptedWasteTypes,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        Enterprise enterprise = enterpriseRepository.findByUserId(currentUser.getId()).orElse(null);
        if (enterprise == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy thông tin doanh nghiệp!");
            return "redirect:/enterprise/dashboard";
        }

        enterprise.setCompanyName(companyName);
        enterprise.setLicenseNumber(licenseNumber);
        enterprise.setProcessingCapacity(processingCapacity);
        enterprise.setServiceArea(serviceArea);

        // Lưu danh sách loại rác tiếp nhận
        if (acceptedWasteTypes != null && !acceptedWasteTypes.isEmpty()) {
            enterprise.setAcceptedWasteTypes(String.join(", ", acceptedWasteTypes));
        } else {
            enterprise.setAcceptedWasteTypes(null);
        }

        enterpriseRepository.save(enterprise);
        ra.addFlashAttribute("successMsg", "✅ Cập nhật năng lực xử lý thành công!");
        return "redirect:/enterprise/dashboard";
    }

    @PostMapping("/reports/{id}/accept")
    public String acceptReport(@PathVariable Long id, RedirectAttributes ra) {
        wasteReportService.updateStatus(id, "ACCEPTED");
        ra.addFlashAttribute("successMsg", "Đã chấp nhận yêu cầu!");
        return "redirect:/enterprise/dashboard";
    }

    @PostMapping("/reports/{id}/reject")
    public String rejectReport(@PathVariable Long id, RedirectAttributes ra) {
        wasteReportService.updateStatus(id, "REJECTED");
        ra.addFlashAttribute("errorMsg", "Đã từ chối yêu cầu!");
        return "redirect:/enterprise/dashboard";
    }

    @PostMapping("/reports/{reportId}/assign/{collectorId}")
    public String assignCollector(@PathVariable Long reportId,
                                   @PathVariable Long collectorId,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        User currentUser = (User) session.getAttribute("currentUser");
        Enterprise enterprise = enterpriseRepository.findByUserId(currentUser.getId()).orElse(null);
        Collector collector = collectorRepository.findById(collectorId).orElse(null);
        WasteReport report = wasteReportService.findById(reportId).orElse(null);

        if (enterprise != null && collector != null && report != null) {
            Assignment assignment = new Assignment();
            assignment.setWasteReport(report);
            assignment.setCollector(collector);
            assignment.setEnterprise(enterprise);
            assignment.setStatus("ASSIGNED");
            assignment.setAssignedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
            wasteReportService.updateStatus(reportId, "ASSIGNED");
            ra.addFlashAttribute("successMsg", "Đã phân công collector!");
        }
        return "redirect:/enterprise/dashboard";
    }

    @PostMapping("/assign")
    public String assignSimple(@RequestParam Long reportId,
                               @RequestParam(required = false) Long collectorId,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (collectorId == null) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn collector!");
            return "redirect:/enterprise/dashboard";
        }

        User currentUser = (User) session.getAttribute("currentUser");
        Enterprise enterprise = enterpriseRepository.findByUserId(currentUser.getId()).orElse(null);
        Collector collector = collectorRepository.findById(collectorId).orElse(null);
        WasteReport report = wasteReportService.findById(reportId).orElse(null);

        if (enterprise != null && collector != null && report != null) {
            Assignment assignment = new Assignment();
            assignment.setWasteReport(report);
            assignment.setCollector(collector);
            assignment.setEnterprise(enterprise);
            assignment.setStatus("ASSIGNED");
            assignment.setAssignedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
            wasteReportService.updateStatus(reportId, "ASSIGNED");
            ra.addFlashAttribute("successMsg", "Đã phân công thành công!");
        } else {
            ra.addFlashAttribute("errorMsg", "Lỗi phân công, thử lại!");
        }
        return "redirect:/enterprise/dashboard";
    }

    @PostMapping("/complaints/{id}/respond")
    public String respondComplaint(@PathVariable Long id,
                                    @RequestParam String response,
                                    @RequestParam String status,
                                    RedirectAttributes ra) {
        complaintRepository.findById(id).ifPresent(complaint -> {
            complaint.setAdminResponse(response);
            complaint.setStatus(status);
            if ("RESOLVED".equals(status)) {
                complaint.setResolvedAt(LocalDateTime.now());
            }
            complaintRepository.save(complaint);
        });
        ra.addFlashAttribute("successMsg", "Đã phản hồi khiếu nại!");
        return "redirect:/enterprise/dashboard";
    }

    // API real-time progress
    @GetMapping("/api/progress")
    @ResponseBody
    public java.util.Map<String, Object> getProgress() {
        List<WasteReport> all = wasteReportService.getAllReports();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("total",     all.size());
        data.put("pending",   all.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        data.put("assigned",  all.stream().filter(r -> "ASSIGNED".equals(r.getStatus())).count());
        data.put("onTheWay",  all.stream().filter(r -> "ON_THE_WAY".equals(r.getStatus())).count());
        data.put("collected", all.stream().filter(r -> "COLLECTED".equals(r.getStatus())).count());
        data.put("rejected",  all.stream().filter(r -> "REJECTED".equals(r.getStatus())).count());
        data.put("updatedAt", java.time.LocalDateTime.now().toString());
        return data;
    }
}