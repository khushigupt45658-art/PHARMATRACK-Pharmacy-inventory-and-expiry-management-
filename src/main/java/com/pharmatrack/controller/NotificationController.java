package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.NotificationDto;
import com.pharmatrack.model.User;
import com.pharmatrack.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(@AuthenticationPrincipal User user) {
        String role = user != null ? user.getRole().name() : null;
        Long userId = user != null ? user.getId() : null;
        List<NotificationDto> notifications = notificationService.getNotificationsForUser(role, userId);
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", notifications));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal User user) {
        String role = user != null ? user.getRole().name() : null;
        Long userId = user != null ? user.getId() : null;
        long count = notificationService.getUnreadCount(role, userId);
        return ResponseEntity.ok(ApiResponse.ok("Unread count", Map.of("count", count)));
    }
}
