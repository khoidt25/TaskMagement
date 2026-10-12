package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.lib.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {


    Optional<Task> findByTaskCode(String taskCode);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByCustomerId(Long customerId, Pageable pageable);

    Page<Task> findByCreatedBy(Long userId, Pageable pageable);

    Page<Task> findByRedmineIssueId(Long redmineIssueId, Pageable pageable);

    Page<Task> findByDeadlineBeforeAndStatusNotIn(LocalDate date, List<TaskStatus> statuses, Pageable pageable);

    boolean existsByProjectId(Long projectId);
}

