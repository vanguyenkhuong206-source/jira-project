package com.controller;

import com.entity.*;
import com.repository.*;
import com.service.UserService;
import com.service.WasteReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final WasteReportService wasteReportService;
    private final EnterpriseRepository enterpriseRepository;
    private final CollectorRepository collectorRepository;
    private final ComplaintRepository complaintRepository;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService,
                           WasteReportService wasteReportService,
                           EnterpriseRepository enterpriseRepository,
                           CollectorRepository collectorRepository,
                           ComplaintRepository complaintRepository,
                           RoleRepository roleRepository) {
        this.userService = userService;
        this.wasteReportService = wasteReportService;
        this.enterpriseRepository = enterpriseRepository;
        this.collectorRepository = collectorRepository;
        this.complaintRepository = complaintRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            org.springframework.security.core.Authentication authentication) {
        
        // Lấy từ session trước
        User currentAdmin = (User) session.getAttribute("currentUser");
        
        // Nếu session null thì lấy từ Security context
        if (currentAdmin == null && authentication != null) {
            currentAdmin = userService.findByUsername(authentication.getName()).orElse(null);
            if (currentAdmin != null) {
                session.setAttribute("currentUser", currentAdmin);
            }
        }

        List<User> allUsers = userService.findAll();
        List<WasteReport> allReports = wasteReportService.getAllReports();
        List<Complaint> allComplaints = complaintRepository.findAllByOrderByCreatedAtDesc();
        List<Enterprise> allEnterprises = enterpriseRepository.findAll();

        long openComplaintsCount = allComplaints.stream()
                .filter(c -> "OPEN".equals(c.getStatus()) || "IN_REVIEW".equals(c.getStatus()))
                .count();

        long totalCitizen = allUsers.stream()
                .filter(u -> "CITIZEN".equals(u.getRole().getName())).count();
        long totalCollector = allUsers.stream()
                .filter(u -> "COLLECTOR".equals(u.getRole().getName())).count();
        long totalEnterprise = allUsers.stream()
                .filter(u -> "ENTERPRISE".equals(u.getRole().getName())).count();

        model.addAttribute("currentAdmin", currentAdmin);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalReports", allReports.size());
        model.addAttribute("allReports", allReports);
        model.addAttribute("recentReports", allReports.stream().limit(10).toList());
        model.addAttribute("pendingReports",
                allReports.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        model.addAttribute("collectedReports",
                allReports.stream().filter(r -> "COLLECTED".equals(r.getStatus())).count());
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allComplaints", allComplaints);
        model.addAttribute("openComplaintsCount", openComplaintsCount);
        model.addAttribute("allEnterprises", allEnterprises);
        model.addAttribute("totalCitizen", totalCitizen);
        model.addAttribute("totalCollector", totalCollector);
        model.addAttribute("totalEnterprise", totalEnterprise);

        return "admin/dashboard";
    }

    @PostMapping("/reports/{id}/status")
    public String updateReportStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     RedirectAttributes ra) {
        wasteReportService.updateStatus(id, status);
        ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id, RedirectAttributes ra) {
        userService.findById(id).ifPresent(user -> {
            user.setIsActive(!user.getIsActive());
            userService.save(user);
        });
        ra.addFlashAttribute("successMsg", "Cập nhật tài khoản thành công!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id,
                                  @RequestParam String roleName,
                                  RedirectAttributes ra) {
        userService.findById(id).ifPresent(user -> {
            roleRepository.findByName(roleName).ifPresent(role -> {
                user.setRole(role);
                userService.save(user);
            });
        });
        ra.addFlashAttribute("successMsg", "Đổi vai trò thành công!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/complaints/{id}/resolve")
    public String resolveComplaint(@PathVariable Long id,
                                    @RequestParam String status,
                                    @RequestParam String adminResponse,
                                    RedirectAttributes ra) {
        complaintRepository.findById(id).ifPresent(complaint -> {
            complaint.setStatus(status);
            complaint.setAdminResponse(adminResponse);
            if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
                complaint.setResolvedAt(LocalDateTime.now());
            }
            complaintRepository.save(complaint);
        });
        ra.addFlashAttribute("successMsg", "Đã xử lý khiếu nại thành công!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/enterprises/{id}/verify")
    public String verifyEnterprise(@PathVariable Long id, RedirectAttributes ra) {
        enterpriseRepository.findById(id).ifPresent(e -> {
            e.setIsVerified(!e.getIsVerified());
            enterpriseRepository.save(e);
        });
        ra.addFlashAttribute("successMsg", "Cập nhật xác minh doanh nghiệp thành công!");
        return "redirect:/admin/dashboard";
    }

    // ===== TẠO ADMIN MỚI =====
    @PostMapping("/create-admin")
    public String createAdmin(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String fullName,
                               @RequestParam(required = false) String phone,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               RedirectAttributes ra) {

        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMsg", "Mật khẩu không khớp!");
            return "redirect:/admin/dashboard";
        }

        if (userService.existsByUsername(username)) {
            ra.addFlashAttribute("errorMsg", "Username '" + username + "' đã tồn tại!");
            return "redirect:/admin/dashboard";
        }

        if (userService.existsByEmail(email)) {
            ra.addFlashAttribute("errorMsg", "Email '" + email + "' đã được sử dụng!");
            return "redirect:/admin/dashboard";
        }

        try {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ADMIN role!"));

            User newAdmin = new User();
            newAdmin.setUsername(username);
            newAdmin.setEmail(email);
            newAdmin.setFullName(fullName);
            newAdmin.setPhone(phone);
            newAdmin.setPassword(new BCryptPasswordEncoder().encode(password));
            newAdmin.setRole(adminRole);
            newAdmin.setIsActive(true);
            newAdmin.setRewardPoints(0);
            userService.save(newAdmin);

            ra.addFlashAttribute("successMsg",
                    "✅ Đã tạo Admin '" + username + "' thành công! Password: " + password);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
}