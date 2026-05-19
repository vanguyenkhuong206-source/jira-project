package com.security;

import com.entity.User;
import com.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔐 Đang đăng nhập: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Không tìm thấy username: " + username);
                    return new UsernameNotFoundException("Không tìm thấy user: " + username);
                });

        System.out.println("✅ Tìm thấy user: " + user.getUsername());
        System.out.println("   isActive = " + user.getIsActive());
        System.out.println("   role     = " + (user.getRole() != null ? user.getRole().getName() : "NULL"));
        System.out.println("   password = " + user.getPassword().substring(0, 20) + "...");

        // Fix null check
        if (user.getIsActive() == null || !user.getIsActive()) {
            System.out.println("❌ Tài khoản bị khóa: " + username);
            throw new UsernameNotFoundException("Tài khoản đã bị khóa!");
        }

        if (user.getRole() == null) {
            System.out.println("❌ Role null cho user: " + username);
            throw new UsernameNotFoundException("Tài khoản chưa được gán quyền!");
        }

        String roleName = "ROLE_" + user.getRole().getName();
        System.out.println("✅ Đăng nhập thành công: " + username + " | role: " + roleName);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}