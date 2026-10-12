package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.ProjectOptionResponse;
import com.example.tasksmanagement.dto.UserOptionResponse;
import com.example.tasksmanagement.entity.AppRole;
import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.repository.AppRoleRepository;
import com.example.tasksmanagement.repository.ProjectRepository;
import com.example.tasksmanagement.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AssignmentOptionService {

    private final UserRepository userRepository;
    private final AppRoleRepository appRoleRepository;
    private final ProjectRepository projectRepository;

    public AssignmentOptionService(
            UserRepository userRepository,
            AppRoleRepository appRoleRepository,
            ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.appRoleRepository = appRoleRepository;
        this.projectRepository = projectRepository;
    }

    public List<UserOptionResponse> getManagers() {
        return getUsersByRole("MANAGER");
    }

    public List<UserOptionResponse> getMembers() {
        return getUsersByRole("MEMBER");
    }

    private List<UserOptionResponse> getUsersByRole(String roleCode) {
        AppRole role = appRoleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Không tìm thấy Role: " + roleCode
                ));

        return userRepository
                .findByRoleIdAndUserStatusIgnoreCase(
                        role.getRoleId(), "ACTIVE")
                .stream()
                .map(this::toUserOption)
                .toList();
    }

    public List<ProjectOptionResponse> getProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::toProjectOption)
                .toList();
    }

    private UserOptionResponse toUserOption(User user) {
        return new UserOptionResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail()
        );
    }

    private ProjectOptionResponse toProjectOption(Project project) {
        return new ProjectOptionResponse(
                project.getProjectId(),
                project.getProjectCode(),
                project.getProjectName()
        );
    }
}




