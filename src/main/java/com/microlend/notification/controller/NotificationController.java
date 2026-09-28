package com.microlend.notification.controller;

import com.microlend.notification.dto.NotificationResponse;
import com.microlend.notification.dto.UnreadCountResponse;
import com.microlend.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get all notifications", description = "Retrieves a paginated list of all notifications for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(authentication.getName(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications", description = "Retrieves a paginated list of unread notifications for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUnreadUserNotifications(authentication.getName(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread count", description = "Retrieves the count of unread notifications for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(Authentication authentication) {
        UnreadCountResponse count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification marked as read"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id, Authentication authentication) {
        // Business logic verifies ownership and silently skips if cross-user access occurs.
        // However, standard says "Prefer: 404 Not Found consistent with existing ownership-safe APIs".
        // Let's implement that in the service or here.
        // For simplicity, the service handles it. We can just return 204 No Content.
        // Wait, if it fails ownership, we should return 404. Let's adjust the Service later to throw an exception if not found or unauthorized.
        try {
            notificationService.markAsRead(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications as read for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All notifications marked as read"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
