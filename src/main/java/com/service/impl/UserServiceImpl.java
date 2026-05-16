package com.service.impl;

import com.dto.request.RegisterRequest;
import com.entity.*;
import com.repository.*;
import com.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CollectorRepository collectorRepository;
    private final EnterpriseRepository enterpriseRepository;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           CollectorRepository collectorRepository,
                           EnterpriseRepository enterpriseRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.collectorRepository = collectorRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username đã tồn tại!");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email đã được sử dụng!");

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Vai trò không hợp lệ!"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(role);
        user.setIsActive(true);
        user.setRewardPoints(0);

        User saved = userRepository.save(user);

        // Tự động tạo record theo role
        if ("COLLECTOR".equals(request.getRole())) {
            Collector collector = new Collector();
            collector.setUser(saved);
            collector.setIsAvailable(true);
            collector.setTotalCollections(0);
            collectorRepository.save(collector);
        } else if ("ENTERPRISE".equals(request.getRole())) {
            Enterprise enterprise = new Enterprise();
            enterprise.setUser(saved);
            enterprise.setCompanyName(request.getFullName());
            enterprise.setIsVerified(false);
            enterpriseRepository.save(enterprise);
        }

        return saved;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void addRewardPoints(Long userId, int points) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRewardPoints(user.getRewardPoints() + points);
            userRepository.save(user);
        });
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}