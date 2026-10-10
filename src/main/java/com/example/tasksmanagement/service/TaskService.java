package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.repository.TaskRepository;
import com.example.tasksmanagement.specification.TaskSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Get all tasks with pagination
     */
    public Page<Task> getAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    /**
     * Get task by ID
     */
    public Optional<Task> getById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Get task by task code
     */
    public Optional<Task> getByCode(String taskCode) {
        return taskRepository.findByTaskCode(taskCode);
    }

    /**
     * Get tasks by status with pagination
     */
    public Page<Task> getByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable);
    }

    /**
     * Get tasks by project with pagination
     */
    public Page<Task> getByProject(Long projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable);
    }

    /**
     * Get tasks by customer with pagination
     */
    public Page<Task> getByCustomer(Long customerId, Pageable pageable) {
        return taskRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Get tasks by creator with pagination
     */
    public Page<Task> getByUser(Long userId, Pageable pageable) {
        return taskRepository.findByCreatedBy(userId, pageable);
    }

    /**
     * Get tasks by Redmine issue with pagination
     */
    public Page<Task> getByRedmine(Long issueId, Pageable pageable) {
        return taskRepository.findByRedmineIssueId(issueId, pageable);
    }

    /**
     * Get overdue tasks with pagination
     */
    public Page<Task> getOverdue(Pageable pageable) {
        return taskRepository.findByDeadlineBeforeAndStatusNotIn(LocalDate.now(), List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED), pageable);
    }

    /**
     * Search tasks by keyword.
     * Searches task code, task name and description.
     */
    public Page<Task> search(String keyword, Pageable pageable) {

        Specification<Task> specification = TaskSpecification.keyword(keyword);

        return taskRepository.findAll(specification, pageable);
    }

    /**
     * Filter tasks using multiple optional conditions.
     */
    public Page<Task> filter(String keyword, TaskStatus status, Long projectId, Long customerId, Long userId, LocalDate deadlineFrom, LocalDate deadlineTo, Pageable pageable) {

        Specification<Task> specification = TaskSpecification.keyword(keyword).and(TaskSpecification.status(status)).and(TaskSpecification.projectId(projectId)).and(TaskSpecification.customerId(customerId)).and(TaskSpecification.createdBy(userId)).and(TaskSpecification.deadlineFrom(deadlineFrom)).and(TaskSpecification.deadlineTo(deadlineTo));

        return taskRepository.findAll(specification, pageable);
    }

    /**
     * Create task
     */
    public Task create(Task task) {
        task.setTaskId(null);
        return taskRepository.save(task);
    }

    /**
     * Update task
     */
    public Optional<Task> update(Long id, Task request) {

        return taskRepository.findById(id).map(task -> {

            task.setTaskCode(request.getTaskCode());
            task.setTaskName(request.getTaskName());
            task.setProjectId(request.getProjectId());
            task.setCustomerId(request.getCustomerId());
            task.setCreatedBy(request.getCreatedBy());
            task.setRedmineIssueId(request.getRedmineIssueId());
            task.setStartDate(request.getStartDate());
            task.setDeadline(request.getDeadline());
            task.setReleaseDate(request.getReleaseDate());
            task.setPriorityId(request.getPriorityId());
            task.setStatus(request.getStatus());
            task.setStatusSource(request.getStatusSource());
            task.setDescription(request.getDescription());
            task.setNote(request.getNote());

            return taskRepository.save(task);
        });
    }

    /**
     * Delete task
     */
    public boolean delete(Long id) {

        if (!taskRepository.existsById(id)) {
            return false;
        }

        taskRepository.deleteById(id);
        return true;
    }
}

