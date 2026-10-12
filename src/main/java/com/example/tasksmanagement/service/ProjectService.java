package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.repository.ProjectRepository;
import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectOptions() {
        return projectRepository.findAllByOrderByProjectNameAsc().stream().map(project -> new ProjectResponse(project.getProjectId(), project.getProjectName())).toList();
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjects(String keyword, String status, Pageable pageable) {
        return projectRepository.searchProjects(keyword, status, pageable);
    }

}
