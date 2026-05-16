package com.controller;

import com.entity.*;
import com.repository.*;
import com.service.WasteReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/enterprise")
public class EnterpriseController {

    private final WasteReportService wasteReportService;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorRepository collectorRepository;
    private final AssignmentRepository assignmentRepository;
    private final ComplaintRepository complaintRepository;
    private final WasteReportRepository wasteReportRepository;

    public EnterpriseController(WasteReportService wasteReportService,
                                 EnterpriseRepository enterpriseRepository,
                                 CollectorRepository collectorRepository,
                                 AssignmentRepository assignmentRepository,
                                 ComplaintRepository complaintRepository,
                                 WasteReportRepository wasteReportRepository) {
        this.wasteReportService = wasteReportService;
        this.enterpriseRepository = enterpriseRepository;
        this.collectorRepository = collectorRepository;
        this.assignmentRepository = assignmentRepository;
        this.complaintRepository = complaintRepository;
        this.wasteReportRepository = wasteReportRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/auth/login";

        Enterprise enterprise = enterpriseRepository.findByUserId(currentUser.getId()).orElse(null);
        List<WasteReport> pendingReports  = wasteReportService.getReportsByStatus("PENDING");
        List<WasteReport> acceptedReports = wasteReportService.getReportsByStatus("ACCEPTED");

        // Khiếu nại
        List<Complaint> allComplaints  = complaintRepository.findAllByOrderByCreatedAtDesc();
        List<Complaint> openComplaints = allComplaints.stream()
                .filter(c -> "OPEN".equals(c.getStatus()) || "IN_REVIEW".equals(c.getStatus()))
                .toList();

        // ── Theo dõi tiến độ ──────────────────────────────────────────────
        List<WasteReport> allReports = wasteReportService.getAllReports();

        long totalReports    = allReports.size();
        long pendingCount    = allReports.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long assignedCount   = allReports.stream().filter(r -> "ASSIGNED".equals(r.getStatus())).count();
        long onTheWayCount   = allReports.stream().filter(r -> "ON_THE_WAY".equals(r.getStatus())).count();
        long collectedCount  = allReports.stream().filter(r -> "COLLECTED".equals(r.getStatus())).count();
        long rejectedCount   = allReports.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();

        // Assignments đang hoạt động (để theo dõi real-time)
        List<Assignment> activeAssignments = assignmentRepository.findAll().stream()
                .filter(a -> !"COMPLETED".equals(a.getStatus()) && !"FAILED".equals(a.getStatus()))
                .sorted(Comparator.comparing(Assignment::getAssignedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // ── Thống kê khối lượng ───────────────────────────────────────────
        // Theo loại rác
        Map<String, Double> weightByType = allReports.stream()
                .filter(r -> "COLLECTED".equals(r.getStatus()) && r.getQuantityKg() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getWasteType().getName(),
                        Collectors.summingDouble(WasteReport::getQuantityKg)));

        // Theo tháng (6 tháng gần nhất)
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MM/yyyy");
        Map<String, Double> weightByMonth = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String key = month.format(monthFmt);
            weightByMonth.put(key, allReports.stream()
                    .filter(r -> "COLLECTED".equals(r.getStatus())
                            && r.getQuantityKg() != null
                            && r.getCreatedAt() != null
                            && r.getCreatedAt().getMonth() == month.getMonth()
                            && r.getCreatedAt().getYear() == month.getYear())
                    .mapToDouble(WasteReport::getQuantityKg).sum());
        }

        // Tổng kg đã thu gom
        double totalKg = allReports.stream()
                .filter(r -> "COLLECTED".equals(r.getStatus()) && r.getQuantityKg() != null)
                .mapToDouble(WasteReport::getQuantityKg).sum();

        model.addAttribute("enterprise", enterprise);
        model.addAttribute("pendingReports", pendingReports);
        model.addAttribute("acceptedReports", acceptedReports);
        model.addAttribute("user", currentUser);
        model.addAttribute("allComplaints", allComplaints);
        model.addAttribute("openComplaints", openComplaints);
        model.addAttribute("collectors", collectorRepository.findAll());

        // Tiến độ
        model.addAttribute("totalReports",   totalReports);
        model.addAttribute("pendingCount",   pendingCount);
        model.addAttribute("assignedCount",  assignedCount);
        model.addAttribute("onTheWayCount",  onTheWayCount);
        model.addAttribute("collectedCount", collectedCount);
        model.addAttribute("rejectedCount",  rejectedCount);
        model.addAttribute("activeAssignments", activeAssignments);

        // Thống kê
        model.addAttribute("weightByType",  weightByType);
        model.addAttribute("weightByMonth", weightByMonth);
        model.addAttribute("totalKg",       totalKg);

        return "enterprise/dashboard";
    }

    // ── API real-time (gọi bằng JS mỗi 30 giây) ──────────────────────────
    @GetMapping("/api/progress")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProgress() {
        List<WasteReport> all = wasteReportService.getAllReports();
        Map<String, Object> data = new HashMap<>();
        data.put("pending",   all.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        data.put("assigned",  all.stream().filter(r -> "ASSIGNED".equals(r.getStatus())).count());
        data.put("onTheWay",  all.stream().filter(r -> "ON_THE_WAY".equals(r.getStatus())).count());
        data.put("collected", all.stream().filter(r -> "COLLECTED".equals(r.getStatus())).count());
        data.put("rejected",  all.stream().filter(r -> "REJECTED".equals(r.getStatus())).count());
        data.put("total",     all.size());
        data.put("updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
        return ResponseEntity.ok(data);
    }

    // ── Các endpoint cũ giữ nguyên ────────────────────────────────────────
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
        Collector collector   = collectorRepository.findById(collectorId).orElse(null);
        WasteReport report    = wasteReportService.findById(reportId).orElse(null);
        if (enterprise != null && collector != null && report != null) {
            Assignment a = new Assignment();
            a.setWasteReport(report); a.setCollector(collector);
            a.setEnterprise(enterprise); a.setStatus("ASSIGNED");
            assignmentRepository.save(a);
            wasteReportService.updateStatus(reportId, "ASSIGNED");
            ra.addFlashAttribute("successMsg", "Đã phân công collector!");
        }
        return "redirect:/enterprise/dashboard";
    }

    @PostMapping("/assign")
    public String assignSimple(@RequestParam Long reportId,
                               @RequestParam(required = false) Long collectorId,
                               HttpSession session, RedirectAttributes ra) {
        if (collectorId == null) {
            ra.addFlashAttribute("errorMsg", "Vui lòng chọn collector!");
            return "redirect:/enterprise/dashboard";
        }
        User currentUser   = (User) session.getAttribute("currentUser");
        Enterprise ent     = enterpriseRepository.findByUserId(currentUser.getId()).orElse(null);
        Collector collector = collectorRepository.findById(collectorId).orElse(null);
        WasteReport report  = wasteReportService.findById(reportId).orElse(null);
        if (ent != null && collector != null && report != null) {
            Assignment a = new Assignment();
            a.setWasteReport(report); a.setCollector(collector);
            a.setEnterprise(ent); a.setStatus("ASSIGNED");
            assignmentRepository.save(a);
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
        complaintRepository.findById(id).ifPresent(c -> {
            c.setAdminResponse(response);
            c.setStatus(status);
            if ("RESOLVED".equals(status)) c.setResolvedAt(LocalDateTime.now());
            complaintRepository.save(c);
        });
        ra.addFlashAttribute("successMsg", "Đã phản hồi khiếu nại!");
        return "redirect:/enterprise/dashboard";
    }
}