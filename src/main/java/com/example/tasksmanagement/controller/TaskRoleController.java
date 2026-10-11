package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.entity.TaskRole;
import com.example.tasksmanagement.service.TaskRoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task-roles")
public class TaskRoleController {

    private final TaskRoleService taskRoleService;

    public TaskRoleController(TaskRoleService taskRoleService) {
        this.taskRoleService = taskRoleService;
    }

    @GetMapping("/active")
    public Page<TaskRole> getActiveTaskRoles(@PageableDefault(page = 0, size = 10, sort = "taskRoleId", direction = Sort.Direction.ASC) Pageable pageable) {
        return taskRoleService.getActiveTaskRoles(pageable);
    }

    @GetMapping
    public Page<TaskRole> getAllTaskRoles(@PageableDefault(page = 0, size = 10, sort = "taskRoleId", direction = Sort.Direction.ASC) Pageable pageable) {
        return taskRoleService.getAllTaskRoles(pageable);
    }

    @GetMapping("/{id}")
    public TaskRole getTaskRoleById(@PathVariable Long id) {
        return taskRoleService.getTaskRoleById(id);
    }
}