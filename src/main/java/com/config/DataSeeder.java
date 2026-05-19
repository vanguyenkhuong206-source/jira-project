package com.config;

import com.entity.Role;
import com.entity.User;
import com.entity.WasteType;
import com.repository.RoleRepository;
import com.repository.UserRepository;
import com.repository.WasteTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final WasteTypeRepository wasteTypeRepository;

    public DataSeeder(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      WasteTypeRepository wasteTypeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.wasteTypeRepository = wasteTypeRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // ── 1. TẠO ROLES ─────────────────────────────────────────────────
        checkAndCreateRole("ADMIN",      "Quản trị viên hệ thống");
        checkAndCreateRole("CITIZEN",    "Người dân");
        checkAndCreateRole("COLLECTOR",  "Nhân viên thu gom");
        checkAndCreateRole("ENTERPRISE", "Doanh nghiệp tái chế");

        // ── 2. TẠO TÀI KHOẢN MẶC ĐỊNH ────────────────────────────────────
        createUserIfNotExists("admin",       "admin@waste.vn",           "200622", "ADMIN",   "0999999999", "ADMIN");
        createUserIfNotExists("citizen1",    "citizen1@gmail.com",       "200622", "Nguyễn Văn An",     "0901234567", "CITIZEN");
        createUserIfNotExists("enterprise1", "enterprise1@recycle.vn",   "200622", "Trần Thị Bình",     "0912345678", "ENTERPRISE");
        createUserIfNotExists("collector1",  "collector1@gmail.com",     "200622", "Lê Văn Cường",      "0923456789", "COLLECTOR");

        // ── 3. TẠO LOẠI RÁC ──────────────────────────────────────────────
        createWasteTypeIfNotExists("Chai nhựa",       "RECYCLABLE", "Chai nhựa PET, HDPE",          15);
        createWasteTypeIfNotExists("Giấy carton",     "RECYCLABLE", "Giấy, bìa carton",              10);
        createWasteTypeIfNotExists("Lon kim loại",    "RECYCLABLE", "Lon nhôm, lon sắt",             12);
        createWasteTypeIfNotExists("Thủy tinh",       "RECYCLABLE", "Chai lọ thủy tinh",             10);
        createWasteTypeIfNotExists("Rác hữu cơ",     "ORGANIC",    "Thức ăn thừa, rau củ",          8);
        createWasteTypeIfNotExists("Pin/Ắc quy",      "HAZARDOUS",  "Pin, ắc quy, thiết bị điện tử", 25);
        createWasteTypeIfNotExists("Hóa chất",        "HAZARDOUS",  "Thuốc trừ sâu, dung môi",       30);
        createWasteTypeIfNotExists("Rác thông thường","GENERAL",    "Rác sinh hoạt hỗn hợp",         5);

        System.out.println("=================================================");
        System.out.println("✅ KHỞI TẠO HỆ THỐNG HOÀN TẤT!");
        System.out.println("👑 Tài khoản mặc định (password: 200622):");
        System.out.println("   admin       / 200622  → ADMIN");
        System.out.println("   citizen1    / 200622  → CITIZEN");
        System.out.println("   enterprise1 / 200622  → ENTERPRISE");
        System.out.println("   collector1  / 200622  → COLLECTOR");
        System.out.println("=================================================");
    }

    // ── Tạo role nếu chưa có ─────────────────────────────────────────────
    private void checkAndCreateRole(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            System.out.println("➕ Tạo role: " + name);
        }
    }

    // ── Tạo user nếu chưa có ─────────────────────────────────────────────
    private void createUserIfNotExists(String username, String email,
                                       String rawPassword, String fullName,
                                       String phone, String roleName) {
        if (!userRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword)); // ✅ BCrypt tự động
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setRole(role);
            user.setIsActive(true);
            user.setRewardPoints(0);
            userRepository.save(user);
            System.out.println("✅ Tạo user: " + username + " / " + rawPassword);
        }
    }

    // ── Tạo loại rác nếu chưa có ─────────────────────────────────────────
    private void createWasteTypeIfNotExists(String name, String category,
                                             String description, int basePoints) {
        if (wasteTypeRepository.findAll().stream()
                .noneMatch(w -> w.getName().equals(name))) {
            WasteType wt = new WasteType();
            wt.setName(name);
            wt.setCategory(category);
            wt.setDescription(description);
            wt.setBasePoints(basePoints);
            wt.setIsActive(true);
            wasteTypeRepository.save(wt);
            System.out.println("➕ Tạo loại rác: " + name);
        }
    }
}