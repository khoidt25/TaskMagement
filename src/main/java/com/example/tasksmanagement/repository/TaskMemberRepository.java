package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.entity.TaskRole;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.lib.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.example.tasksmanagement.lib.TaskStatus;

public interface TaskMemberRepository extends JpaRepository<TaskMember, Long> {

    List<TaskMember> findByTaskId(Long taskId);

    List<TaskMember> findByUserId(Long userId);

    List<TaskMember> findByUserIdAndStatus(Long userId, TaskStatus status);

    Optional<TaskMember> findByTaskIdAndUserId(Long taskId, Long userId);

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);

    boolean existsByTaskId(Long taskId);

    @Query("""
        SELECT tm
        FROM TaskMember tm
        JOIN User u ON u.userId = tm.userId
        WHERE tm.taskMemberId = :taskMemberId
          AND u.username = :username
        """)
    Optional<TaskMember> findAssignedTaskForUser(
            @Param("taskMemberId") Long taskMemberId,
            @Param("username") String username
    );

    @Query(
            value = """
            SELECT tm, t, u, tr
            FROM TaskMember tm
            LEFT JOIN Task t ON t.taskId = tm.taskId
            LEFT JOIN User u ON u.userId = tm.userId
            LEFT JOIN TaskRole tr ON tr.taskRoleId = tm.taskRoleId
            WHERE tm.userId = :userId
            ORDER BY tm.taskMemberId DESC
            """,
            countQuery = """
            SELECT COUNT(tm)
            FROM TaskMember tm
            WHERE tm.userId = :userId
            """
    )
    Page<Object[]> findMyTasksWithDetails(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(
            value = """
            SELECT tm, t, u, tr
            FROM TaskMember tm
            LEFT JOIN Task t ON t.taskId = tm.taskId
            LEFT JOIN User u ON u.userId = tm.userId
            LEFT JOIN TaskRole tr ON tr.taskRoleId = tm.taskRoleId
            WHERE tm.userId = :userId
              AND tm.status = :status
            ORDER BY tm.taskMemberId DESC
            """,
            countQuery = """
            SELECT COUNT(tm)
            FROM TaskMember tm
            WHERE tm.userId = :userId
              AND tm.status = :status
            """
    )
    Page<Object[]> findMyTasksWithDetailsByStatus(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            Pageable pageable
    );

    // Các thành viên có deadline sắp tới.
    // Deadline còn đúng 1 ngày và công việc chưa hoàn thành/hủy.
    @Query("""
        SELECT tm
        FROM TaskMember tm
        WHERE tm.deadline = :deadline
          AND tm.status NOT IN :excludedStatuses
        ORDER BY tm.deadline ASC
        """)
    List<TaskMember> findMembersWithUpcomingDeadline(
            @Param("deadline") LocalDate deadline,
            @Param("excludedStatuses") List<TaskStatus> excludedStatuses
    );

    // Các thành viên đã quá deadline nhưng công việc chưa hoàn thành/hủy.
    @Query("""
        SELECT tm
        FROM TaskMember tm
        WHERE tm.deadline < :today
          AND tm.status NOT IN :excludedStatuses
        ORDER BY tm.deadline ASC
        """)
    List<TaskMember> findMembersWithOverdueDeadline(
            @Param("today") LocalDate today,
            @Param("excludedStatuses") List<TaskStatus> excludedStatuses
    );

    interface MemberTaskCount {
        Long getMemberId();
        Long getTotalTasks();
        String getMemberName();
    }

    @Query(value = """
        SELECT
            tm.user_id AS "memberId",
            u.full_name AS "memberName",
            COUNT(DISTINCT tm.task_id) AS "totalTasks"
        FROM task_members tm
        INNER JOIN tasks t
            ON t.task_id = tm.task_id
        INNER JOIN users u
            ON u.user_id = tm.user_id
        WHERE t.project_id = :projectId
        GROUP BY tm.user_id, u.full_name
        ORDER BY tm.user_id
        """, nativeQuery = true)
    List<MemberTaskCount> countTasksByMemberInProject(
            @Param("projectId") Long projectId
    );
}