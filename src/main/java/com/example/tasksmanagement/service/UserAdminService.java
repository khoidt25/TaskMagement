package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.UserCreateRequest;
import com.example.tasksmanagement.dto.UserResponse;
import com.example.tasksmanagement.entity.AppRole;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.repository.AppRoleRepository;
import com.example.tasksmanagement.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, AppRoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppRole role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new IllegalArgumentException("Role not found"));

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRoleId(role.getRoleId());
        user.setUserStatus("ACTIVE");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        AppRole role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found"));

        user.setRoleId(role.getRoleId());
        user.setUpdatedAt(OffsetDateTime.now());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeStatus(Long userId, String status) {
        if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUserStatus(status.toUpperCase());
        user.setUpdatedAt(OffsetDateTime.now());

        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRoleId(user.getRoleId());
        response.setUserStatus(user.getUserStatus());

        if (user.getRoleId() != null) {
            roleRepository.findById(user.getRoleId()).ifPresent(role -> response.setRoleCode(role.getRoleCode()));
        }

        return response;
    }
}

