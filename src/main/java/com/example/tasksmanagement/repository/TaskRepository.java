package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.dto.CustomerTaskSummaryResponse;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.lib.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByCustomerId(Long customerId);

    long countByStatus(TaskStatus status);

    List<Task> findByCreatedBy(Long createdBy);

    long countDistinctByProjectId(Long projectId);

    @Query("""
    SELECT new com.example.tasksmanagement.dto.CustomerTaskSummaryResponse(
        c.customerId,
        c.customerName,
        COUNT(t),
        SUM(CASE WHEN t.status = com.example.tasksmanagement.lib.TaskStatus.IN_PROGRESS
                 THEN 1L ELSE 0L END),
        SUM(CASE WHEN t.status = com.example.tasksmanagement.lib.TaskStatus.COMPLETED
                 THEN 1L ELSE 0L END),
        SUM(CASE WHEN t.deadline < CURRENT_DATE
                  AND t.status NOT IN (
                      com.example.tasksmanagement.lib.TaskStatus.COMPLETED,
                      com.example.tasksmanagement.lib.TaskStatus.CANCELLED
                  )
                 THEN 1L ELSE 0L END)
    )
    FROM Task t, Customer c
    WHERE t.customerId = c.customerId
      AND t.project.projectId IN (
          SELECT mp.projectId
          FROM ManagerProject mp
          WHERE mp.managerId = :managerId
      )
    GROUP BY c.customerId, c.customerName
    ORDER BY c.customerName
    """)
    List<CustomerTaskSummaryResponse> findCustomerTaskSummaryByManagerId(
            @Param("managerId") Long managerId
    );

}

