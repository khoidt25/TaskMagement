
package com.example.tasksmanagement.service;

import com.example.tasksmanagement.entity.Notification;
import com.example.tasksmanagement.entity.TaskMember;
import com.example.tasksmanagement.repository.TaskMemberRepository;
import com.example.tasksmanagement.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import com.example.tasksmanagement.lib.TaskStatus;

@Service
public class TaskDeadlineNotificationService {

    private static final int UPCOMING_DAYS = 3;

    private static final List<TaskStatus> EXCLUDED_STATUSES =
            List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED);

    private final TaskMemberRepository taskMemberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public TaskDeadlineNotificationService(
            TaskMemberRepository taskMemberRepository,
            NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.taskMemberRepository = taskMemberRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Tạo thông báo khi deadline còn từ hôm nay đến 3 ngày nữa.
     */
    @Transactional
    public int notifyUpcomingDeadlines() {
        LocalDate today = LocalDate.now();
        int created = 0;

        for (int i = 0; i <= UPCOMING_DAYS; i++) {
            LocalDate deadline = today.plusDays(i);

            List<TaskMember> members =
                    taskMemberRepository.findMembersWithUpcomingDeadline(
                            deadline, EXCLUDED_STATUSES);

            for (TaskMember member : members) {
                if (member.getUserId() == null
                        || member.getTaskId() == null) {
                    continue;
                }

                if (notificationRepository
                        .existsByUserIdAndTaskIdAndNotificationType(
                                member.getUserId(),
                                member.getTaskId(),
                                "DEADLINE_SOON")) {
                    continue;
                }

                String message;

                if (deadline.equals(today)) {
                    message = "Công việc của bạn đến hạn hôm nay.";
                } else {
                    long daysLeft = java.time.temporal.ChronoUnit.DAYS
                            .between(today, deadline);

                    message = "Công việc của bạn còn "
                            + daysLeft
                            + " ngày đến deadline ("
                            + deadline
                            + ").";
                }

                notificationService.createNotification(
                        member.getUserId(),
                        member.getTaskId(),
                        "DEADLINE_SOON",
                        "Công việc sắp đến hạn",
                        message
                );

                created++;
            }
        }

        return created;
    }

    /**
     * Tạo thông báo cho công việc đã quá deadline.
     */
    @Transactional
    public int notifyOverdueTasks() {
        LocalDate today = LocalDate.now();

        List<TaskMember> members =
                taskMemberRepository.findMembersWithOverdueDeadline(
                        today, EXCLUDED_STATUSES);

        int created = 0;

        for (TaskMember member : members) {
            if (member.getUserId() == null
                    || member.getTaskId() == null) {
                continue;
            }

            if (notificationRepository
                    .existsByUserIdAndTaskIdAndNotificationType(
                            member.getUserId(),
                            member.getTaskId(),
                            "TASK_OVERDUE")) {
                continue;
            }

            notificationService.createNotification(
                    member.getUserId(),
                    member.getTaskId(),
                    "TASK_OVERDUE",
                    "Công việc đã quá hạn",
                    "Công việc của bạn đã quá deadline "
                            + member.getDeadline()
                            + ". Vui lòng cập nhật tiến độ."
            );

            created++;
        }

        return created;
    }

    /**
     * Chạy cả hai loại thông báo.
     */
    @Transactional
    public int processDeadlineNotifications() {
        int upcoming = notifyUpcomingDeadlines();
        int overdue = notifyOverdueTasks();

        return upcoming + overdue;
    }
}