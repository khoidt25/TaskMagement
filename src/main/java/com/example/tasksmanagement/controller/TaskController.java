package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.service.TaskService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Get all tasks with pagination
     * <p>
     * Example:
     * GET /api/tasks
     * GET /api/tasks?page=0&size=10
     * GET /api/tasks?page=0&size=20&sort=taskId,desc
     */
    @GetMapping
    public Page<Task> getAll(@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {

        return taskService.getAll(pageable);
    }

    /**
     * Get task by ID
     * <p>
     * GET /api/tasks/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {

        return taskService.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get task by task code
     * <p>
     * GET /api/tasks/code/TASK-001
     */
    @GetMapping("/code/{taskCode}")
    public ResponseEntity<Task> getByCode(@PathVariable String taskCode) {

        return taskService.getByCode(taskCode).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get tasks by status
     * <p>
     * GET /api/tasks/status/IN_PROGRESS
     * <p>
     * Supports pagination.
     */
    @GetMapping("/status/{status}")
    public Page<Task> getByStatus(@PathVariable TaskStatus status,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {

        return taskService.getByStatus(status, pageable);
    }

    /**
     * Get tasks by project
     * <p>
     * GET /api/tasks/project/1
     */
    @GetMapping("/project/{projectId}")
    public Page<Task> getByProject(@PathVariable Long projectId,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {

        return taskService.getByProject(projectId, pageable);
    }

    /**
     * Get tasks by customer
     * <p>
     * GET /api/tasks/customer/1
     */
    @GetMapping("/customer/{customerId}")
    public Page<Task> getByCustomer(@PathVariable Long customerId,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {

        return taskService.getByCustomer(customerId, pageable);
    }

    /**
     * Get tasks created by user
     * <p>
     * GET /api/tasks/user/1
     */
    @GetMapping("/user/{userId}")
    public Page<Task> getByUser(@PathVariable Long userId,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {

        return taskService.getByUser(userId, pageable);
    }

    /**
     * Get tasks by Redmine issue
     * <p>
     * GET /api/tasks/redmine/16574
     */
    @GetMapping("/redmine/{issueId}")
    public Page<Task> getByRedmine(@PathVariable Long issueId,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {

        return taskService.getByRedmine(issueId, pageable);
    }

    /**
     * Get overdue tasks
     * <p>
     * GET /api/tasks/overdue
     */
    @GetMapping("/overdue")
    public Page<Task> getOverdue(@ParameterObject @PageableDefault(page = 0, size = 10, sort = "deadline", direction = Sort.Direction.ASC) Pageable pageable) {

        return taskService.getOverdue(pageable);
    }

    /**
     * Create task
     * <p>
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {

        return ResponseEntity.ok(taskService.create(task));
    }

    /**
     * Update task
     * <p>
     * PUT /api/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody Task request) {

        return taskService.update(id, request).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete task
     * <p>
     * DELETE /api/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        if (!taskService.delete(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Search tasks by keyword. * * Example: * GET /api/tasks/search?keyword=login&page=0&size=10
     */
    @GetMapping("/search")
    public Page<Task> search(@RequestParam(required = false) String keyword,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {
        return taskService.search(keyword, pageable);
    }

    /**
     * Filter tasks using multiple conditions. * * Example: * GET /api/tasks/filter?status=IN_PROGRESS&projectId=1
     */
    @GetMapping("/filter")
    public Page<Task> filter(@RequestParam(required = false) String keyword, @RequestParam(required = false) TaskStatus status, @RequestParam(required = false) Long projectId, @RequestParam(required = false) Long customerId, @RequestParam(required = false) Long userId, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate deadlineFrom, @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate deadlineTo,@ParameterObject @PageableDefault(page = 0, size = 10, sort = "taskId", direction = Sort.Direction.DESC) Pageable pageable) {
        if (deadlineFrom != null && deadlineTo != null && deadlineFrom.isAfter(deadlineTo)) {
            throw new IllegalArgumentException("deadlineFrom must be before or equal to deadlineTo");
        }
        return taskService.filter(keyword, status, projectId, customerId, userId, deadlineFrom, deadlineTo, pageable);
    }

}