package com.microlend.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Count of unread notifications")
public class UnreadCountResponse {

    @Schema(description = "Total number of unread notifications for the user", example = "5")
    private long unreadCount;

    public UnreadCountResponse() {
    }

    public UnreadCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
