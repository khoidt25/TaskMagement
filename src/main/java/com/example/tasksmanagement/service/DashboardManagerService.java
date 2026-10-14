package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Project;
import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.repository.ProjectRepository;
import com.example.tasksmanagement.repository.TaskRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardManagerService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public DashboardManagerService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Lấy task do Manager đang đăng nhập tạo.
     *
     * Yêu cầu TaskRepository có:
     * List<Task> findByCreatedBy(Long createdBy);
     */
    private List<Task> getManagerTasks(Long managerId) {
        return taskRepository.findByCreatedBy(managerId);
    }

    /**
     * GET /api/dashboard/summary
     */
    public Map<String, Object> getSummary(Long managerId) {
        List<Task> tasks = getManagerTasks(managerId);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("totalTasks", tasks.size());
        result.put("inProgressTasks",
                countByStatus(tasks, TaskStatus.IN_PROGRESS));
        result.put("completedTasks",
                countByStatus(tasks, TaskStatus.COMPLETED));
        result.put("overdueTasks",
                tasks.stream().filter(this::isOverdue).count());

        return result;
    }

    /**
     * GET /api/dashboard/task-status
     */
    public Map<String, Object> getTaskStatus(Long managerId) {
        List<Task> tasks = getManagerTasks(managerId);

        Map<String, Long> statusCounts = getStatusCounts(tasks);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTasks", tasks.size());
        result.put("statusCounts", statusCounts);

        return result;
    }

    /**
     * GET /api/dashboard/statistics/status
     */
    public Map<String, Long> getTaskStatusStatistics(Long managerId) {
        return getStatusCounts(getManagerTasks(managerId));
    }

    /**
     * GET /api/dashboard/project-summary
     */
    public List<Map<String, Object>> getProjectSummary(Long managerId) {
        List<Task> tasks = getManagerTasks(managerId);

        Map<Long, List<Task>> grouped = tasks.stream()
                .filter(task -> task.getProjectId() != null)
                .collect(Collectors.groupingBy(
                        Task::getProjectId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Map<String, Object>> result = new ArrayList<>();

        grouped.forEach((projectId, projectTasks) -> {
            Map<String, Object> item = new LinkedHashMap<>();

            String projectName = projectRepository.findById(projectId)
                    .map(Project::getProjectName)
                    .orElse("Unknown Project");

            item.put("projectId", projectId);
            item.put("projectName", projectName);
            item.put("totalTasks", projectTasks.size());
            item.put("inProgressTasks",
                    countByStatus(projectTasks, TaskStatus.IN_PROGRESS));
            item.put("completedTasks",
                    countByStatus(projectTasks, TaskStatus.COMPLETED));
            item.put("overdueTasks",
                    projectTasks.stream()
                            .filter(this::isOverdue)
                            .count());

            result.add(item);
        });

        return result;
    }

    private Map<String, Long> getStatusCounts(List<Task> tasks) {
        Map<String, Long> result = new LinkedHashMap<>();

        for (TaskStatus status : TaskStatus.values()) {
            result.put(
                    status.name(),
                    countByStatus(tasks, status)
            );
        }

        return result;
    }

    private long countByStatus(
            List<Task> tasks,
            TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    private boolean isOverdue(Task task) {
        return task.getDeadline() != null
                && task.getDeadline().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.CANCELLED;
    }

}
