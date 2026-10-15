
package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardManagerRepository
        extends JpaRepository<Task, Long> {

    /**
     * Danh sách task quá hạn thuộc các dự án
     * được gán cho Manager trong manager_projects.
     */
    @Query("""
    SELECT DISTINCT t
    FROM Task t
    WHERE t.project.projectId IN (
        SELECT mp.projectId
        FROM ManagerProject mp
        WHERE mp.managerId = :managerId
    )
    AND t.deadline IS NOT NULL
    AND t.deadline < CURRENT_DATE
    AND t.status NOT IN (
        com.example.tasksmanagement.lib.TaskStatus.COMPLETED,
        com.example.tasksmanagement.lib.TaskStatus.CANCELLED
    )
    ORDER BY t.deadline ASC, t.taskId DESC
    """)
    List<Task> findOverdueTasksByManagerId(
            @Param("managerId") Long managerId
    );
}

