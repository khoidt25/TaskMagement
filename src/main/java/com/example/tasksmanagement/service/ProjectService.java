package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.ProjectRequest;
import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.repository.ProjectRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjects(String keyword, String status, Pageable pageable) {
        return projectRepository.searchProjects(keyword, status, pageable);
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy dự án ID: " + id));
    }

    // Thêm dự án
    @Transactional
    public Project createProject(ProjectRequest request) {
        validateProject(request);

        String code = normalizeCode(request.getProjectCode());

        if (projectRepository.existsByProjectCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã dự án đã tồn tại: " + code);
        }

        Project project = new Project();
        applyRequest(project, request);
        project.setProjectCode(code);

        if (request.getProjectStatus() == null || request.getProjectStatus().isBlank()) {
            project.setProjectStatus("ACTIVE");
        }

        return projectRepository.save(project);
    }

    // Cập nhật dự án
    @Transactional
    public Project updateProject(Long id, ProjectRequest request) {
        validateProject(request);

        Project project = getProjectById(id);
        String code = normalizeCode(request.getProjectCode());

        if (projectRepository.existsByProjectCodeAndProjectIdNot(code, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã dự án đã được sử dụng: " + code);
        }

        applyRequest(project, request);
        project.setProjectCode(code);

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        project.setProjectStatus("INACTIVE");
        projectRepository.save(project);
    }

    // Danh sách dự án cho dropdown
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectOptions() {
        return projectRepository.findByProjectStatus("ACTIVE").stream().sorted((a, b) -> a.getProjectName().compareToIgnoreCase(b.getProjectName())).map(project -> new ProjectResponse(project.getProjectId(), project.getProjectName())).toList();
    }

    private void validateProject(ProjectRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu dự án không được để trống");
        }

        if (request.getProjectCode() == null || request.getProjectCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã dự án không được để trống");
        }

        if (request.getProjectCode().trim().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã dự án tối đa 50 ký tự");
        }

        if (request.getProjectName() == null || request.getProjectName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên dự án không được để trống");
        }

        if (request.getProjectName().trim().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên dự án tối đa 200 ký tự");
        }

        if (request.getProjectStatus() != null && !request.getProjectStatus().isBlank()) {
            String status = request.getProjectStatus().trim().toUpperCase(Locale.ROOT);

            if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái chỉ được là ACTIVE hoặc INACTIVE");
            }
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setProjectName(request.getProjectName().trim());
        project.setDescription(request.getDescription());

        if (request.getProjectStatus() != null && !request.getProjectStatus().isBlank()) {
            project.setProjectStatus(request.getProjectStatus().trim().toUpperCase(Locale.ROOT));
        }
    }
}
