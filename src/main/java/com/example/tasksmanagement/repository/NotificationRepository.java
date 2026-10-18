
package com.example.tasksmanagement.repository;

import com.example.tasksmanagement.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    @Modifying
    @Query("""
        UPDATE Notification n
           SET n.read = true,
               n.readAt = CURRENT_TIMESTAMP
         WHERE n.notificationId = :notificationId
           AND n.userId = :userId
           AND n.read = false
        """)
    int markAsRead(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId);

    @Modifying
    @Query("""
        UPDATE Notification n
           SET n.read = true,
               n.readAt = CURRENT_TIMESTAMP
         WHERE n.userId = :userId
           AND n.read = false
        """)
    int markAllAsRead(@Param("userId") Long userId);

    boolean existsByUserIdAndTaskIdAndNotificationType(
            Long userId,
            Long taskId,
            String notificationType
    );
}
