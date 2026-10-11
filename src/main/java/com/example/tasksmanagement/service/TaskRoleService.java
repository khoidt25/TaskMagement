package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.TaskRole;
import com.example.tasksmanagement.repository.TaskRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskRoleService {

    private final TaskRoleRepository taskRoleRepository;

    public TaskRoleService(TaskRoleRepository taskRoleRepository) {
        this.taskRoleRepository = taskRoleRepository;
    }

    public Page<TaskRole> getActiveTaskRoles(Pageable pageable) {
        return taskRoleRepository.findByActiveTrue(pageable);
    }

    public Page<TaskRole> getAllTaskRoles(Pageable pageable) {
        return taskRoleRepository.findAll(pageable);
    }

    public TaskRole getTaskRoleById(Long taskRoleId) {
        return taskRoleRepository.findById(taskRoleId).orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + taskRoleId));
    }
}