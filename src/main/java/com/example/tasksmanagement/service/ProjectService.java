package com.example.tasksmanagement.service;

import com.example.tasksmanagement.repository.ProjectRepository;
import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectOptions() {
        return projectRepository.findAllByOrderByProjectNameAsc()
                .stream()
                .map(project -> new ProjectResponse(
                        project.getProjectId(),
                        project.getProjectName()
                ))
                .toList();
    }
}
