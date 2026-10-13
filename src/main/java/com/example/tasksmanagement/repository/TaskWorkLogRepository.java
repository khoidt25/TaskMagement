
package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.dto.TaskWorkLogProjection;
import com.example.tasksmanagement.entity.TaskWorkLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskWorkLogRepository
        extends JpaRepository<TaskWorkLog, Long> {

    @Query("""
        SELECT
            wl.workLogId AS workLogId,
            tm.taskMemberId AS taskMemberId,
            t.taskId AS taskId,
            t.taskCode AS taskCode,
            t.taskName AS taskName,
            wl.workDate AS workDate,
            wl.plannedHours AS plannedHours,
            wl.actualHours AS actualHours,
            wl.note AS note,
            wl.createdAt AS createdAt,
            wl.updatedAt AS updatedAt
        FROM TaskWorkLog wl
        JOIN wl.taskMember tm
        JOIN Task t ON t.taskId = tm.taskId
        JOIN User u ON u.userId = tm.userId
        WHERE u.username = :username
          AND wl.workDate = :workDate
        ORDER BY t.taskName ASC
        """)
    Page<TaskWorkLogProjection> findLogsByDate(
            @Param("username") String username,
            @Param("workDate") LocalDate workDate,
            Pageable pageable
    );

    @Query("""
        SELECT
            wl.workLogId AS workLogId,
            tm.taskMemberId AS taskMemberId,
            t.taskId AS taskId,
            t.taskCode AS taskCode,
            t.taskName AS taskName,
            wl.workDate AS workDate,
            wl.plannedHours AS plannedHours,
            wl.actualHours AS actualHours,
            wl.note AS note,
            wl.createdAt AS createdAt,
            wl.updatedAt AS updatedAt
        FROM TaskWorkLog wl
        JOIN wl.taskMember tm
        JOIN Task t ON t.taskId = tm.taskId
        JOIN User u ON u.userId = tm.userId
        WHERE u.username = :username
          AND wl.workDate BETWEEN :from AND :to
        ORDER BY wl.workDate ASC, t.taskName ASC
        """)
    Page<TaskWorkLogProjection> findLogsByDateRange(
            @Param("username") String username,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    Optional<TaskWorkLog>
    findByTaskMember_TaskMemberIdAndWorkDate(
            Long taskMemberId,
            LocalDate workDate
    );
}
