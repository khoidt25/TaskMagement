package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<Page<Project>> getProjects(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status, @PageableDefault(size = 10) @SortDefault.SortDefaults({@SortDefault(sort = "projectName", direction = Sort.Direction.ASC)}) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjects(keyword, status, pageable));
    }

}