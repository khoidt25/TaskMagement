
package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.dto.OverdueTaskProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface OverdueTaskRepository
        extends Repository<com.example.tasksmanagement.entity.TaskMember, Long> {


    @Query(
            value = """
            SELECT
                tm.task_member_id AS "taskMemberId",
                t.task_id AS "taskId",
                t.task_code AS "taskCode",
                t.task_name AS "taskName",
                u.user_id AS "userId",
                u.username AS "username",
                u.full_name AS "fullName",
                tm.assigned_work AS "assignedWork",
                tm.status AS "status",
                tm.progress AS "progress",
                COALESCE(tm.deadline, t.deadline) AS "deadline",
                p.project_name AS "projectName",
                pr.priority_name AS "priorityName",
                (CURRENT_DATE - COALESCE(tm.deadline, t.deadline))
                    AS "overdueDays"
            FROM manager_members mm
            JOIN task_members tm
                ON tm.user_id = mm.member_id
            JOIN tasks t
                ON t.task_id = tm.task_id
            JOIN users u
                ON u.user_id = tm.user_id
            LEFT JOIN projects p
                ON p.project_id = t.project_id
            LEFT JOIN priorities pr
                ON pr.priority_id = t.priority_id
            WHERE mm.manager_id = :managerId
              AND COALESCE(tm.deadline, t.deadline) < CURRENT_DATE
              AND tm.status NOT IN ('COMPLETED', 'CANCELLED')
            ORDER BY COALESCE(tm.deadline, t.deadline) ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM manager_members mm
            JOIN task_members tm
                ON tm.user_id = mm.member_id
            JOIN tasks t
                ON t.task_id = tm.task_id
            WHERE mm.manager_id = :managerId
              AND COALESCE(tm.deadline, t.deadline) < CURRENT_DATE
              AND tm.status NOT IN ('COMPLETED', 'CANCELLED')
            """,
            nativeQuery = true
    )
    Page<OverdueTaskProjection> findManagerOverdueTasks(
            @Param("managerId") Long managerId,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT
                tm.task_member_id AS "taskMemberId",
                t.task_id AS "taskId",
                t.task_code AS "taskCode",
                t.task_name AS "taskName",
                u.user_id AS "userId",
                u.username AS "username",
                u.full_name AS "fullName",
                tm.assigned_work AS "assignedWork",
                tm.status AS "status",
                tm.progress AS "progress",
                COALESCE(tm.deadline, t.deadline) AS "deadline",
                p.project_name AS "projectName",
                pr.priority_name AS "priorityName",
                (CURRENT_DATE - COALESCE(tm.deadline, t.deadline))
                    AS "overdueDays"
            FROM task_members tm
            JOIN tasks t
                ON t.task_id = tm.task_id
            JOIN users u
                ON u.user_id = tm.user_id
            LEFT JOIN projects p
                ON p.project_id = t.project_id
            LEFT JOIN priorities pr
                ON pr.priority_id = t.priority_id
            WHERE tm.user_id = :memberId
              AND COALESCE(tm.deadline, t.deadline) < CURRENT_DATE
              AND tm.status NOT IN ('COMPLETED', 'CANCELLED')
            ORDER BY COALESCE(tm.deadline, t.deadline) ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM task_members tm
            JOIN tasks t
                ON t.task_id = tm.task_id
            WHERE tm.user_id = :memberId
              AND COALESCE(tm.deadline, t.deadline) < CURRENT_DATE
              AND tm.status NOT IN ('COMPLETED', 'CANCELLED')
            """,
            nativeQuery = true
    )
    Page<OverdueTaskProjection> findMemberOverdueTasks(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
