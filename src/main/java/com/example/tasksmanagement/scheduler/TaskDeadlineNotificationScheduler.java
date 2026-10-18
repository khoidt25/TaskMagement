
package com.example.tasksmanagement.scheduler;

import com.example.tasksmanagement.service.TaskDeadlineNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TaskDeadlineNotificationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(TaskDeadlineNotificationScheduler.class);

    private final TaskDeadlineNotificationService deadlineNotificationService;

    public TaskDeadlineNotificationScheduler(
            TaskDeadlineNotificationService deadlineNotificationService) {
        this.deadlineNotificationService = deadlineNotificationService;
    }

    // Chạy tự động lúc 08:00 mỗi ngày theo giờ Việt Nam
//
//    0 0 8 * * *
    @Scheduled(
            cron = "0 * * * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    public void checkDeadlines() {
        try {
            int count = deadlineNotificationService
                    .processDeadlineNotifications();

            log.info("Đã tạo {} thông báo deadline.", count);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra deadline và tạo thông báo.", e);
        }
    }
}
