
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.ProjectRequest;
import com.example.tasksmanagement.dto.ProjectResponse;
import com.example.tasksmanagement.entity.AppRole;
import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.repository.AppRoleRepository;
import com.example.tasksmanagement.repository.ManagerProjectRepository;
import com.example.tasksmanagement.repository.ProjectRepository;
import com.example.tasksmanagement.repository.TaskRepository;

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
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final AppRoleRepository appRoleRepository;
    private final ManagerProjectRepository managerProjectRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            AppRoleRepository appRoleRepository,
            ManagerProjectRepository managerProjectRepository) {

        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.appRoleRepository = appRoleRepository;
        this.managerProjectRepository = managerProjectRepository;
    }

    private String getCurrentRole() {
        User user = currentUserService.getCurrentUser();

        if (user.getRoleId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Tài khoản chưa được gán Role");
        }

        AppRole role = appRoleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Role không tồn tại"));

        return role.getRoleCode() == null
                ? ""
                : role.getRoleCode().trim().toUpperCase(Locale.ROOT);
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(getCurrentRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ ADMIN được phép quản lý Project");
        }
    }

    private void requireProjectAccess(Long projectId) {
        String role = getCurrentRole();

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("MANAGER".equals(role)) {
            Long managerId = currentUserService.getCurrentUserId();

            boolean assigned = managerProjectRepository
                    .existsByManagerIdAndProjectId(managerId, projectId);

            if (assigned) {
                return;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền truy cập Project này");
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjects(
            String keyword, String status, Pageable pageable) {

        String role = getCurrentRole();

        if ("ADMIN".equals(role)) {
            return projectRepository.searchProjects(
                    keyword, status, pageable);
        }

        if ("MANAGER".equals(role)) {
            Long managerId = currentUserService.getCurrentUserId();

            return projectRepository.searchProjectsByManager(
                    managerId, keyword, status, pageable);
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền xem danh sách Project");
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Project ID: " + id));

        requireProjectAccess(id);
        return project;
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        requireAdmin();
        validateProject(request);

        String code = normalizeCode(request.getProjectCode());

        if (projectRepository.existsByProjectCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã dự án đã tồn tại: " + code);
        }

        Project project = new Project();
        applyRequest(project, request);
        project.setProjectCode(code);

        if (request.getProjectStatus() == null
                || request.getProjectStatus().isBlank()) {
            project.setProjectStatus("ACTIVE");
        }

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, ProjectRequest request) {
        requireAdmin();
        validateProject(request);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy Project ID: " + id));

        String code = normalizeCode(request.getProjectCode());

        if (projectRepository.existsByProjectCodeAndProjectIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã dự án đã được sử dụng: " + code);
        }

        applyRequest(project, request);
        project.setProjectCode(code);

        return projectRepository.save(project);
    }

    @Transactional
    public boolean deleteProject(Long id) {
        requireAdmin();

        // Kiểm tra ID của Project, không phải ID của Task.
        if (id == null || !projectRepository.existsById(id)) {
            return false;
        }

        if (taskRepository.existsByProjectId(id)) {
            throw new IllegalStateException(
                    "Không thể xóa project đã có công việc.");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy Project"));

        project.setProjectStatus("INACTIVE");
        projectRepository.save(project);

        return true;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectOptions() {
        String role = getCurrentRole();

        List<Project> projects;

        if ("ADMIN".equals(role)) {
            projects = projectRepository.findByProjectStatus("ACTIVE")
                    .stream()
                    .sorted((a, b) -> a.getProjectName()
                            .compareToIgnoreCase(b.getProjectName()))
                    .toList();

        } else if ("MANAGER".equals(role)) {
            Long managerId = currentUserService.getCurrentUserId();

            projects = projectRepository
                    .findActiveProjectsByManager(managerId);

        } else {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bạn không có quyền xem danh sách Project");
        }

        return projects.stream()
                .map(p -> new ProjectResponse(
                        p.getProjectId(), p.getProjectName()))
                .toList();
    }

    private void validateProject(ProjectRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu dự án không được để trống");
        }

        if (request.getProjectCode() == null
                || request.getProjectCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã dự án không được để trống");
        }

        if (request.getProjectCode().trim().length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã dự án tối đa 50 ký tự");
        }

        if (request.getProjectName() == null
                || request.getProjectName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên dự án không được để trống");
        }

        if (request.getProjectName().trim().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên dự án tối đa 200 ký tự");
        }

        if (request.getProjectStatus() != null
                && !request.getProjectStatus().isBlank()) {
            String status = request.getProjectStatus()
                    .trim().toUpperCase(Locale.ROOT);

            if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Trạng thái chỉ được là ACTIVE hoặc INACTIVE");
            }
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setProjectName(request.getProjectName().trim());
        project.setDescription(request.getDescription());

        if (request.getProjectStatus() != null
                && !request.getProjectStatus().isBlank()) {
            project.setProjectStatus(
                    request.getProjectStatus()
                            .trim().toUpperCase(Locale.ROOT));
        }
    }
}
