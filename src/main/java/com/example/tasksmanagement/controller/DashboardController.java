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

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/task-status")
    public ResponseEntity<Map<String, Object>> getTaskStatus() {
        return ResponseEntity.ok(dashboardService.getTaskStatus());
    }

    @GetMapping("/my-summary")
    public ResponseEntity<Map<String, Object>> getMySummary(@RequestParam Long userId) {

        return ResponseEntity.ok(dashboardService.getMySummary(userId));
    }

    @GetMapping("/project-summary")
    public ResponseEntity<List<Map<String, Object>>> getProjectSummary() {
        return ResponseEntity.ok(dashboardService.getProjectSummary());
    }

    @GetMapping("/statistics/status")
    public ResponseEntity<Map<String, Long>> getTaskStatusStatistics() {
        return ResponseEntity.ok(dashboardService.getTaskStatusStatistics());
    }
}

