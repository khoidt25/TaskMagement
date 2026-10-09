package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.UserCreateRequest;
import com.example.tasksmanagement.dto.UserResponse;
import com.example.tasksmanagement.service.UserAdminService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAdminService userAdminService;

    public UserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userAdminService.getAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserCreateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long userId, @RequestParam Long roleId) {
        try {
            return ResponseEntity.ok(userAdminService.changeRole(userId, roleId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long userId, @RequestParam String status) {
        try {
            return ResponseEntity.ok(userAdminService.changeStatus(userId, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

