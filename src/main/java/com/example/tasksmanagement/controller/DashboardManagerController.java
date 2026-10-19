package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.CustomerTaskSummaryResponse;
import com.example.tasksmanagement.dto.ProjectMemberTaskSummaryResponse;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.service.DashboardManagerService;
import com.example.tasksmanagement.service.TaskService;
import com.example.tasksmanagement.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboardManager")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardManagerController {

    private final DashboardManagerService dashboardManagerService;
    private final UserAdminService userAdminService;
    private final TaskService taskService;

    public DashboardManagerController(
            DashboardManagerService dashboardManagerService,
            UserAdminService userAdminService, TaskService taskService) {
        this.dashboardManagerService = dashboardManagerService;
        this.userAdminService = userAdminService;
        this.taskService = taskService;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);
        return ResponseEntity.ok(
                dashboardManagerService.getSummary(managerId)
        );
    }

    @GetMapping("/task-status")
    public ResponseEntity<Map<String, Object>> getTaskStatus(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);
        return ResponseEntity.ok(
                dashboardManagerService.getTaskStatus(managerId)
        );
    }

    @GetMapping("/statistics/status")
    public ResponseEntity<Map<String, Long>> getTaskStatusStatistics(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);
        return ResponseEntity.ok(
                dashboardManagerService.getTaskStatusStatistics(managerId)
        );
    }

    @GetMapping("/project-summary")
    public ResponseEntity<List<Map<String, Object>>> getProjectSummary(
            Authentication authentication) {

        Long managerId = getManagerId(authentication);
        return ResponseEntity.ok(
                dashboardManagerService.getProjectSummary(managerId)
        );
    }

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

    @GetMapping("/overdue-tasks")
    public ResponseEntity<List<Task>> getOverdueTasks(
            Authentication authentication) {
        Long managerId = getManagerId(authentication);

        return ResponseEntity.ok(
                dashboardManagerService.getOverdueTasks(managerId)
        );
    }

    @GetMapping("/project-member-task-summary")
    public ResponseEntity<ProjectMemberTaskSummaryResponse>
    getProjectMemberTaskSummary(
            @RequestParam Long managerId) {
        return ResponseEntity.ok(dashboardManagerService.getProjectMemberTaskSummary(managerId));
    }

    @GetMapping("/customer-summary")
    public List<CustomerTaskSummaryResponse> getCustomerTaskSummary(
            Authentication authentication) {
        Long managerId = userAdminService.getUserIdByUsername(
                authentication.getName()
        );
        return taskService.getCustomerTaskSummaryByManagerId(managerId);
    }

}
