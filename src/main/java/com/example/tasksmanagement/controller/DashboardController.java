package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Tổng quan tất cả task.
     * GET /api/dashboard/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    /**
     * Số lượng task theo trạng thái.
     * GET /api/dashboard/task-status
     */
    @GetMapping("/task-status")
    public ResponseEntity<Map<String, Object>> getTaskStatus() {
        return ResponseEntity.ok(dashboardService.getTaskStatus());
    }

    /**
     * Tổng hợp task được giao cho user.
     * GET /api/dashboard/my-summary?userId=3
     */
    @GetMapping("/my-summary")
    public ResponseEntity<Map<String, Object>> getMySummary(@RequestParam Long userId) {

        return ResponseEntity.ok(dashboardService.getMySummary(userId));
    }

    /**
     * Thống kê task theo project.
     * GET /api/dashboard/project-summary
     */
    @GetMapping("/project-summary")
    public ResponseEntity<List<Map<String, Object>>> getProjectSummary() {
        return ResponseEntity.ok(dashboardService.getProjectSummary());
    }
}

