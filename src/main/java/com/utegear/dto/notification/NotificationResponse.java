package com.utegear.dto.notification;

import com.utegear.entity.Notification;
import java.time.OffsetDateTime;

public record NotificationResponse(Long id, String type, String title, String message, boolean read,
                                   String referenceId, OffsetDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getType().name(), notification.getTitle(),
                notification.getMessage(), notification.isRead(), notification.getReferenceId(),
                notification.getCreatedAt());
    }
}
