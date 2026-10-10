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

    // Tìm task theo mã
    Optional<Task> findByTaskCode(String taskCode);

    // Lọc theo trạng thái
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    // Lọc theo dự án
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    // Lọc theo khách hàng
    Page<Task> findByCustomerId(Long customerId, Pageable pageable);

    // Lọc theo người tạo task
    Page<Task> findByCreatedBy(Long userId, Pageable pageable);

    // Lọc theo Redmine Issue
    Page<Task> findByRedmineIssueId(Long redmineIssueId, Pageable pageable);

    // Lọc task quá hạn, chưa hoàn thành hoặc chưa hủy
    Page<Task> findByDeadlineBeforeAndStatusNotIn(LocalDate date, List<TaskStatus> statuses, Pageable pageable);
}

