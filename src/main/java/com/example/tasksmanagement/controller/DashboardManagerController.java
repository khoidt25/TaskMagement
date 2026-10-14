package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.service.DashboardService;
import com.example.tasksmanagement.service.UserAdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('MANAGER')")

public class DashboardManagerController {
    private final DashboardService dashboardService;
    private final UserAdminService userAdminService;

    public DashboardManagerController(
            DashboardManagerService dashboardService,
            UserAdminService userAdminService) {
        this.dashboardService = dashboardService;
        this.userAdminService = userAdminService;
    }

    /**
     * Thống kê tổng quan task của Manager đang đăng nhập.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardService.getSummary(managerId)
        );
    }

    /**
     * Thống kê task theo trạng thái.
     */
    @GetMapping("/task-status")
    public ResponseEntity<Map<String, Object>> getTaskStatus(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardService.getTaskStatus(managerId)
        );
    }

    /**
     * Thống kê số lượng task theo từng trạng thái.
     */
    @GetMapping("/statistics/status")
    public ResponseEntity<Map<String, Long>> getTaskStatusStatistics(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardService.getTaskStatusStatistics(managerId)
        );
    }

    /**
     * Thống kê dự án có task do Manager tạo.
     */
    @GetMapping("/project-summary")
    public ResponseEntity<List<Map<String, Object>>> getProjectSummary(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardService.getProjectSummary(managerId)
        );
    }

    private Long getManagerId(Authentication authentication) {
        return userAdminService.getUserIdByUsername(
                authentication.getName()
        );
    }
}
