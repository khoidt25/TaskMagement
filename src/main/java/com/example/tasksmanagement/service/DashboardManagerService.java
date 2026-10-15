
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.dto.ProjectMemberTaskSummaryResponse;
import com.example.tasksmanagement.entity.ManagerProject;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.repository.DashboardManagerRepository;
import com.example.tasksmanagement.repository.ManagerProjectRepository;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.TaskRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.metadata.ManagedAttribute;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.example.tasksmanagement.dto.ProjectMemberTaskSummaryResponse.MemberSummary;
import com.example.tasksmanagement.dto.ProjectMemberTaskSummaryResponse.ProjectSummary;

@Service
@Transactional(readOnly = true)
public class DashboardManagerService {

    private final JdbcTemplate jdbcTemplate;
    private DashboardManagerRepository dashboardManagerRepository;

    private final ManagerProjectRepository managerProjectRepository;
    private final TaskRepository taskRepository;
    private final TaskMemberRepository taskMemberRepository;


    public DashboardManagerService(JdbcTemplate jdbcTemplate, DashboardManagerRepository dashboardManagerRepository, TaskRepository taskRepository, TaskMemberRepository taskMemberRepository, ManagerProjectRepository managerProjectRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.dashboardManagerRepository = dashboardManagerRepository;
        this.taskRepository = taskRepository;
        this.taskMemberRepository = taskMemberRepository;
        this.managerProjectRepository = managerProjectRepository;
    }

    /**
     * Thống kê tổng quan theo các dự án được gán
     * cho Manager trong bảng manager_projects.
     */
    public Map<String, Object> getSummary(Long managerId) {
        String sql = """
            SELECT
                COUNT(DISTINCT t.task_id) AS "totalTasks",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'NOT_STARTED')
                    AS "notStarted",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'IN_PROGRESS')
                    AS "inProgress",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'PENDING')
                    AS "pending",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'COMPLETED')
                    AS "completed",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'CANCELLED')
                    AS "cancelled",

                COUNT(DISTINCT t.task_id)
                    FILTER (
                        WHERE t.deadline < CURRENT_DATE
                          AND t.status NOT IN ('COMPLETED', 'CANCELLED')
                    ) AS "overdue",

                (
                    SELECT COUNT(DISTINCT mp.project_id)
                    FROM manager_projects mp
                    WHERE mp.manager_id = ?
                ) AS "totalProjects"

            FROM manager_projects mp
            JOIN projects p
                ON p.project_id = mp.project_id
            LEFT JOIN tasks t
                ON t.project_id = p.project_id
            WHERE mp.manager_id = ?
            """;

        Map<String, Object> result =
                jdbcTemplate.queryForMap(sql, managerId, managerId);

        // Chuẩn hóa kiểu dữ liệu để frontend dễ sử dụng.
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTasks", result.get("totalTasks"));
        summary.put("notStarted", result.get("notStarted"));
        summary.put("inProgress", result.get("inProgress"));
        summary.put("pending", result.get("pending"));
        summary.put("completed", result.get("completed"));
        summary.put("cancelled", result.get("cancelled"));
        summary.put("overdue", result.get("overdue"));
        summary.put("totalProjects", result.get("totalProjects"));

        return summary;
    }

    /**
     * Dữ liệu biểu đồ trạng thái task.
     * Chỉ thống kê task thuộc dự án được gán cho Manager.
     */
    public Map<String, Object> getTaskStatus(Long managerId) {
        Map<String, Long> statistics =
                getTaskStatusStatistics(managerId);

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        addStatus(labels, data, statistics, "NOT_STARTED");
        addStatus(labels, data, statistics, "IN_PROGRESS");
        addStatus(labels, data, statistics, "PENDING");
        addStatus(labels, data, statistics, "COMPLETED");
        addStatus(labels, data, statistics, "CANCELLED");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        result.put("statuses", statistics);

        long total = statistics.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        result.put("total", total);

        return result;
    }

    /**
     * Đếm số task theo từng trạng thái.
     * Luôn trả về đủ các trạng thái, kể cả khi bằng 0.
     */
    public Map<String, Long> getTaskStatusStatistics(Long managerId) {
        String sql = """
            SELECT
                t.status,
                COUNT(DISTINCT t.task_id) AS total
            FROM manager_projects mp
            JOIN projects p
                ON p.project_id = mp.project_id
            JOIN tasks t
                ON t.project_id = p.project_id
            WHERE mp.manager_id = ?
            GROUP BY t.status
            """;

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(sql, managerId);

        Map<String, Long> statistics = new LinkedHashMap<>();

        statistics.put("NOT_STARTED", 0L);
        statistics.put("IN_PROGRESS", 0L);
        statistics.put("PENDING", 0L);
        statistics.put("COMPLETED", 0L);
        statistics.put("CANCELLED", 0L);

        for (Map<String, Object> row : rows) {
            Object statusObject = row.get("status");
            Object totalObject = row.get("total");

            if (statusObject == null || totalObject == null) {
                continue;
            }

            String status = statusObject.toString();
            long total = ((Number) totalObject).longValue();

            if (statistics.containsKey(status)) {
                statistics.put(status, total);
            }
        }

        return statistics;
    }

    /**
     * Thống kê theo từng dự án được gán trong manager_projects.
     * Dự án chưa có task vẫn được trả về với số lượng bằng 0.
     */
    public List<Map<String, Object>> getProjectSummary(Long managerId) {
        String sql = """
            SELECT
                p.project_id AS "projectId",
                p.project_code AS "projectCode",
                p.project_name AS "projectName",

                COUNT(DISTINCT t.task_id) AS "totalTasks",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'NOT_STARTED')
                    AS "notStarted",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'IN_PROGRESS')
                    AS "inProgress",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'PENDING')
                    AS "pending",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'COMPLETED')
                    AS "completed",

                COUNT(DISTINCT t.task_id)
                    FILTER (WHERE t.status = 'CANCELLED')
                    AS "cancelled",

                COUNT(DISTINCT t.task_id)
                    FILTER (
                        WHERE t.deadline < CURRENT_DATE
                          AND t.status NOT IN ('COMPLETED', 'CANCELLED')
                    ) AS "overdue"

            FROM manager_projects mp
            JOIN projects p
                ON p.project_id = mp.project_id
            LEFT JOIN tasks t
                ON t.project_id = p.project_id

            WHERE mp.manager_id = ?

            GROUP BY
                p.project_id,
                p.project_code,
                p.project_name

            ORDER BY p.project_name
            """;

        return jdbcTemplate.queryForList(sql, managerId);
    }

    /**
     * Thêm dữ liệu trạng thái vào dữ liệu biểu đồ.
     */
    private void addStatus(
            List<String> labels,
            List<Long> data,
            Map<String, Long> statistics,
            String status) {

        labels.add(status);
        data.add(statistics.getOrDefault(status, 0L));
    }

    /**
     * Lấy danh sách task quá hạn thuộc các dự án
     * được gán cho Manager trong bảng manager_projects.
     */
    public List<Task> getOverdueTasks(Long managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException(
                    "Manager ID không được để trống."
            );
        }

        return dashboardManagerRepository
                .findOverdueTasksByManagerId(managerId);
    }

    @Transactional(readOnly = true)
    public ProjectMemberTaskSummaryResponse
    getProjectMemberTaskSummary(Long managerId) {

        List<ManagerProject> assignments =
                managerProjectRepository.findByManagerId(managerId);

        // Loại project trùng nếu dữ liệu phân công có bản ghi lặp.
        Set<Long> projectIds = new LinkedHashSet<>();

        for (ManagerProject assignment : assignments) {
            projectIds.add(assignment.getProjectId());
        }

        List<ProjectMemberTaskSummaryResponse.ProjectSummary> projectSummaries = new ArrayList<>();

        for (Long projectId : projectIds) {

            // Tổng task của project: mỗi task chỉ đếm một lần.
            long totalTasks =
                    taskRepository.countDistinctByProjectId(projectId);

            // Thống kê số task của từng member trong project.
            List<MemberSummary> members = taskMemberRepository
                    .countTasksByMemberInProject(projectId)
                    .stream()
                    .map(item -> new MemberSummary(
                            item.getMemberId(),
                            item.getTotalTasks()))
                    .toList();

            projectSummaries.add(
                    new ProjectSummary(projectId, totalTasks, members));
        }

        return new ProjectMemberTaskSummaryResponse(
                managerId, projectSummaries);
    }

}
