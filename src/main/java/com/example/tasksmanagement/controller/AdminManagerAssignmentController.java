package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.AssignMembersRequest;
import com.example.tasksmanagement.dto.AssignProjectsRequest;
import com.example.tasksmanagement.service.CurrentUserService;
import com.example.tasksmanagement.service.ManagerAssignmentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/manager-assignments")
public class AdminManagerAssignmentController {

    private final ManagerAssignmentService service;
    private final CurrentUserService currentUserService;

    public AdminManagerAssignmentController(
            ManagerAssignmentService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignProjects(
            @Valid @RequestBody AssignProjectsRequest request) {

        Long adminId = currentUserService.getCurrentUserId();

        service.assignProjects(request, adminId);

        return ResponseEntity.ok(Map.of(
                "message", "Assign Project thành công",
                "managerId", request.managerId(),
                "projectIds", request.projectIds()
        ));
    }

    @PostMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignMembers(
            @Valid @RequestBody AssignMembersRequest request) {

        Long adminId = currentUserService.getCurrentUserId();

        service.assignMembers(request, adminId);

        return ResponseEntity.ok(Map.of(
                "message", "Assign Member thành công",
                "managerId", request.managerId(),
                "memberIds", request.memberIds()
        ));
    }

    @GetMapping("/{managerId}/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Long> getProjects(@PathVariable Long managerId) {
        return service.getProjectIds(managerId);
    }

    @GetMapping("/{managerId}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Long> getMembers(@PathVariable Long managerId) {
        return service.getMemberIds(managerId);
    }

    @DeleteMapping("/{managerId}/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeProject(
            @PathVariable Long managerId,
            @PathVariable Long projectId) {

        service.removeProject(managerId, projectId);
        return ResponseEntity.ok(
                Map.of("message", "Đã thu hồi quyền quản lý Project"));
    }

    @DeleteMapping("/{managerId}/members/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeMember(
            @PathVariable Long managerId,
            @PathVariable Long memberId) {

        service.removeMember(managerId, memberId);
        return ResponseEntity.ok(
                Map.of("message", "Đã thu hồi quyền quản lý Member"));
    }

}