// UserService.java
package com.service;

import com.dto.request.RegisterRequest;
import com.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(RegisterRequest request);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    void addRewardPoints(Long userId, int points);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}