package com.microlend.notification.dto;

import com.microlend.notification.entity.Notification;
import com.microlend.notification.entity.NotificationType;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a user notification")
public class NotificationResponse {

    @Schema(description = "Unique notification ID", example = "1")
    private Long id;
    
    @Schema(description = "Type of notification", example = "LOAN_APPROVED")
    private NotificationType notificationType;
    
    @Schema(description = "Notification title", example = "Loan Application Approved")
    private String title;
    
    @Schema(description = "Detailed notification message", example = "Your loan application ML-20260913-000024 has been approved.")
    private String message;
    
    @Schema(description = "Indicates whether the user has read this notification", example = "false")
    private boolean isRead;
    
    @Schema(description = "Timestamp when the notification was generated", example = "2026-09-13T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the notification was marked as read", example = "null")
    private LocalDateTime readAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.notificationType = notification.getNotificationType();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
        this.readAt = notification.getReadAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
