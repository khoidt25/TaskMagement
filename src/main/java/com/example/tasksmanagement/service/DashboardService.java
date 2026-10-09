package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Task;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.lib.TaskStatus;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.TaskRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TaskRepository taskRepository;
    private final TaskMemberRepository taskMemberRepository;

    public DashboardService(TaskRepository taskRepository, TaskMemberRepository taskMemberRepository) {
        this.taskRepository = taskRepository;
        this.taskMemberRepository = taskMemberRepository;
    }

    /**
     * GET /api/dashboard/summary
     * Thống kê tổng quan task.
     */
    public Map<String, Object> getSummary() {
        List<Task> tasks = taskRepository.findAll();

        long total = tasks.size();
        long inProgress = countByStatus(tasks, TaskStatus.IN_PROGRESS);
        long completed = countByStatus(tasks, TaskStatus.COMPLETED);
        long overdue = tasks.stream().filter(this::isOverdue).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTasks", total);
        result.put("inProgressTasks", inProgress);
        result.put("completedTasks", completed);
        result.put("overdueTasks", overdue);

        return result;
    }

    /**
     * GET /api/dashboard/task-status
     * Thống kê task theo từng trạng thái.
     */
    public Map<String, Object> getTaskStatus() {
        List<Task> tasks = taskRepository.findAll();

        Map<String, Long> statusCounts = new LinkedHashMap<>();

        for (TaskStatus status : TaskStatus.values()) {
            long count = countByStatus(tasks, status);
            statusCounts.put(status.name(), count);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTasks", tasks.size());
        result.put("statusCounts", statusCounts);

        return result;
    }

    /**
     * GET /api/dashboard/my-summary?userId=3
     * Thống kê task được giao cho một user.
     */
    public Map<String, Object> getMySummary(Long userId) {
        List<TaskMember> members = taskMemberRepository.findByUserId(userId);

        long total = members.size();

        long inProgress = members.stream().filter(m -> m.getStatus() == TaskStatus.IN_PROGRESS).count();

        long completed = members.stream().filter(m -> m.getStatus() == TaskStatus.COMPLETED).count();

        long overdue = members.stream().filter(m -> m.getDeadline() != null).filter(m -> m.getDeadline().isBefore(LocalDate.now())).filter(m -> m.getStatus() != TaskStatus.COMPLETED && m.getStatus() != TaskStatus.CANCELLED).count();

        double averageProgress = total == 0 ? 0.0 : members.stream().mapToInt(m -> m.getProgress() == null ? 0 : m.getProgress()).average().orElse(0.0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("totalTasks", total);
        result.put("inProgressTasks", inProgress);
        result.put("completedTasks", completed);
        result.put("overdueTasks", overdue);
        result.put("averageProgress", Math.round(averageProgress * 100.0) / 100.0);

        return result;
    }

    /**
     * GET /api/dashboard/project-summary
     * Thống kê task theo projectId.
     */
    public List<Map<String, Object>> getProjectSummary() {
        List<Task> tasks = taskRepository.findAll();

        Map<Long, List<Task>> grouped = tasks.stream().filter(t -> t.getProjectId() != null).collect(Collectors.groupingBy(Task::getProjectId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();

        grouped.forEach((projectId, projectTasks) -> {
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("projectId", projectId);
            item.put("totalTasks", projectTasks.size());
            item.put("inProgressTasks", countByStatus(projectTasks, TaskStatus.IN_PROGRESS));
            item.put("completedTasks", countByStatus(projectTasks, TaskStatus.COMPLETED));
            item.put("overdueTasks", projectTasks.stream().filter(this::isOverdue).count());

            result.add(item);
        });

        return result;
    }

    private long countByStatus(List<Task> tasks, TaskStatus status) {
        return tasks.stream().filter(t -> t.getStatus() == status).count();
    }

    private boolean isOverdue(Task task) {
        return task.getDeadline() != null && task.getDeadline().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CANCELLED;
    }
}

