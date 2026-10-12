package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.dto.ProjectRequest;
import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.service.ProjectService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    public ResponseEntity<Page<Project>> getProjects(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status, @PageableDefault(page = 0, size = 10, sort = "projectName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjects(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequest request) {
        Project created = projectService.createProject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/options")
    public ResponseEntity<List<ProjectResponse>> getProjectOptions() {
        return ResponseEntity.ok(projectService.getProjectOptions());
    }
}
