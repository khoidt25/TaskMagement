package com.example.tasksmanagement.controller;

import com.example.tasksmanagement.entity.Notification;
import com.example.tasksmanagement.entity.User;
import com.example.tasksmanagement.repository.UserRepository;
import com.example.tasksmanagement.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }

        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng"));
        return user.getUserId();
    }

    @GetMapping
    public Page<Notification> getMyNotifications(Authentication authentication, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        Long userId = getCurrentUserId(authentication);
        return notificationService.getMyNotifications(userId, page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        return Map.of("unreadCount", notificationService.countUnread(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(Authentication authentication, @PathVariable Long notificationId) {

        Long userId = getCurrentUserId(authentication);
        boolean updated = notificationService.markAsRead(userId, notificationId);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu thông báo là đã đọc"));
    }

    @PatchMapping("/read-all")
    public Map<String, Object> markAllAsRead(Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        int updated = notificationService.markAllAsRead(userId);
        return Map.of("message", "Đã đánh dấu tất cả thông báo là đã đọc", "updatedCount", updated);
    }
}
