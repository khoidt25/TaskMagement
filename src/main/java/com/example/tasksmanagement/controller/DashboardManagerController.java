package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.service.DashboardManagerService;
import com.example.tasksmanagement.service.UserAdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboardManager")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardManagerController {

    private final DashboardManagerService dashboardManagerService;
    private final UserAdminService userAdminService;

    public DashboardManagerController(
            DashboardManagerService dashboardManagerService,
            UserAdminService userAdminService) {
        this.dashboardManagerService = dashboardManagerService;
        this.userAdminService = userAdminService;
    }

    /**
     * Thống kê tổng quan các task thuộc phạm vi dự án
     * được gán cho Manager đang đăng nhập.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardManagerService.getSummary(managerId)
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
                dashboardManagerService.getTaskStatus(managerId)
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
                dashboardManagerService.getTaskStatusStatistics(managerId)
        );
    }

    /**
     * Thống kê task theo các dự án được gán cho Manager
     * trong bảng manager_projects.
     */
    @GetMapping("/project-summary")
    public ResponseEntity<List<Map<String, Object>>> getProjectSummary(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardManagerService.getProjectSummary(managerId)
        );
    }

    /**
     * Lấy ID của Manager đang đăng nhập,
     * không nhận managerId từ request của frontend.
     */
    private Long getManagerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn chưa đăng nhập."
            );
        }

        return userAdminService.getUserIdByUsername(
                authentication.getName()
        );
    }

    /**
     * Danh sách task quá hạn của Manager đang đăng nhập.
     */
    @GetMapping("/overdue-tasks")
    public ResponseEntity<List<Task>> getOverdueTasks(
            Authentication authentication) {

        Long managerId = userAdminService.getUserIdByUsername(
                authentication.getName()
        );

        return ResponseEntity.ok(
                dashboardManagerService.getOverdueTasks(managerId)
        );
    }

}
