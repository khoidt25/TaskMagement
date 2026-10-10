package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/options")
    public List<ProjectResponse> getProjectOptions() {
        return projectService.getProjectOptions();
    }
}