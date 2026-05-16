package com.security;

import com.entity.User;
import com.repository.UserRepository;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomAuthSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            request.getSession().setAttribute("currentUser", user);
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_CITIZEN");

        switch (role) {
            case "ROLE_ADMIN":      response.sendRedirect("/admin/dashboard"); break;
            case "ROLE_ENTERPRISE": response.sendRedirect("/enterprise/dashboard"); break;
            case "ROLE_COLLECTOR":  response.sendRedirect("/collector/dashboard"); break;
            default:                response.sendRedirect("/citizen/dashboard"); break;
        }
    }
}