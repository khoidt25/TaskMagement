package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.ProjectOptionResponse;
import com.example.tasksmanagement.dto.UserOptionResponse;
import com.example.tasksmanagement.service.AssignmentOptionService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AssignmentOptionController {

    private final AssignmentOptionService assignmentOptionService;

    public AssignmentOptionController(
            AssignmentOptionService assignmentOptionService) {
        this.assignmentOptionService = assignmentOptionService;
    }

    @GetMapping("/managers")
    public List<UserOptionResponse> getManagers() {
        return assignmentOptionService.getManagers();
    }

    @GetMapping("/members")
    public List<UserOptionResponse> getMembers() {
        return assignmentOptionService.getMembers();
    }

    @GetMapping("/projects")
    public List<ProjectOptionResponse> getProjects() {
        return assignmentOptionService.getProjects();
    }
}
