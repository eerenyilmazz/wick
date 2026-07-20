package com.wick.notification.dto;

import com.wick.notification.domain.InboxNotificationType;

import java.time.Instant;
import java.util.UUID;

public record InboxNotificationResponse(
        UUID id,
        InboxNotificationType type,
        String title,
        String body,
        boolean read,
        UUID referenceId,
        Instant createdAt
) {
}
