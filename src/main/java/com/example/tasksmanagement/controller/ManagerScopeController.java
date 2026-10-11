package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.service.CurrentUserService;
import com.example.tasksmanagement.service.ManagerAssignmentService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerScopeController {

    private final ManagerAssignmentService service;
    private final CurrentUserService currentUserService;

    public ManagerScopeController(ManagerAssignmentService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/projects")
    public List<Long> getMyProjects() {
        Long managerId = currentUserService.getCurrentUserId();
        return service.getProjectIds(managerId);
    }

    @GetMapping("/members")
    public List<Long> getMyMembers() {
        Long managerId = currentUserService.getCurrentUserId();
        return service.getMemberIds(managerId);
    }

//    private Long currentUserId() {
//        // TODO: Lấy userId từ Authentication/JWT đã xác thực.
//        throw new UnsupportedOperationException(
//                "Hãy tích hợp với JWT authentication hiện tại");
//    }
}