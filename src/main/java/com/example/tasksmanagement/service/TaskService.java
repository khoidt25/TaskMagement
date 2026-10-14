package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.ManagerProject;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.repository.ManagerProjectRepository;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.TaskRepository;
import com.example.tasksmanagement.repository.UserRepository;
import com.example.tasksmanagement.specification.TaskSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final UserRepository userRepository;
    private final ManagerProjectRepository managerProjectRepository;

    public TaskService(TaskRepository taskRepository, TaskMemberRepository taskMemberRepository, UserRepository userRepository, ManagerProjectRepository managerProjectRepository) {

        this.taskRepository = taskRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.userRepository = userRepository;
        this.managerProjectRepository = managerProjectRepository;
    }

    public Page<Task> getAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }

    public Optional<Task> getById(Long id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> getByCode(String taskCode) {
        return taskRepository.findByTaskCode(taskCode);
    }

    public Page<Task> getByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable);
    }

    public Page<Task> getByProject(Long projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable);
    }

    public Page<Task> getByCustomer(Long customerId, Pageable pageable) {
        return taskRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Task> getByUser(Long userId, Pageable pageable) {
        return taskRepository.findByCreatedBy(userId, pageable);
    }

    public Page<Task> getByRedmine(Long issueId, Pageable pageable) {
        return taskRepository.findByRedmineIssueId(issueId, pageable);
    }

    public Page<Task> getOverdue(Pageable pageable) {
        return taskRepository.findByDeadlineBeforeAndStatusNotIn(LocalDate.now(), List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED), pageable);
    }

    public Page<Task> search(String keyword, Pageable pageable) {

        Specification<Task> specification = TaskSpecification.keyword(keyword);

        return taskRepository.findAll(specification, pageable);
    }

    public Page<Task> filter(String keyword, TaskStatus status, Long projectId, Long customerId, Long userId, LocalDate deadlineFrom, LocalDate deadlineTo, Pageable pageable) {

        Specification<Task> specification = TaskSpecification.keyword(keyword).and(TaskSpecification.status(status)).and(TaskSpecification.projectId(projectId)).and(TaskSpecification.customerId(customerId)).and(TaskSpecification.assignedToUser(userId)).and(TaskSpecification.deadlineFrom(deadlineFrom)).and(TaskSpecification.deadlineTo(deadlineTo));

        return taskRepository.findAll(specification, pageable);
    }

    public Page<Task> filterManagerTasks(
            Long managerId,
            String keyword,
            TaskStatus status,
            Long projectId,
            Long customerId,
            Long userId,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            Pageable pageable) {

        // Lấy các dự án được phân công cho Manager.
        List<Long> managerProjectIds =
                managerProjectRepository.findByManagerId(managerId)
                        .stream()
                        .map(ManagerProject::getProjectId)
                        .toList();

        // Manager chưa được phân công dự án nào.
        if (managerProjectIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Specification<Task> specification =
                TaskSpecification.keyword(keyword)
                        .and(TaskSpecification.status(status))
                        .and(TaskSpecification.projectId(projectId))
                        .and(TaskSpecification.customerId(customerId))
                        .and(TaskSpecification.assignedToUser(userId))
                        .and(TaskSpecification.deadlineFrom(deadlineFrom))
                        .and(TaskSpecification.deadlineTo(deadlineTo))
                        // Chỉ lấy task thuộc dự án Manager quản lý.
                        .and((root, query, cb) ->
                                root.get("projectId").in(managerProjectIds));

        return taskRepository.findAll(specification, pageable);
    }

    public Task create(Task task) {
        task.setTaskId(null);
        return taskRepository.save(task);
    }

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

    @Transactional
    public boolean delete(Long id) {
        if (id == null || !taskRepository.existsById(id)) {
            return false;
        }

        boolean assigned = taskMemberRepository.existsByTaskId(id);

        System.out.println(
                "[DELETE TASK] taskId=" + id + ", assigned=" + assigned
        );

        if (assigned) {
            throw new IllegalStateException(
                    "Không thể xóa task đã được phân công cho Member."
            );
        }

        taskRepository.deleteById(id);
        return true;
    }

    public Page<Task> getTasksCreatedByManager(
            String username,
            Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tài khoản"));

        return taskRepository.findByCreatedBy(
                user.getUserId(),
                pageable
        );
    }



}

