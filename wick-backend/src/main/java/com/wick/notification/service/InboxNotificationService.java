package com.wick.notification.service;

import com.wick.common.dto.PageResponse;
import com.wick.notification.domain.InboxNotificationType;
import com.wick.notification.dto.InboxNotificationResponse;
import com.wick.user.domain.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InboxNotificationService {

    PageResponse<InboxNotificationResponse> getInbox(UUID userId, Pageable pageable);

    void markAllRead(UUID userId);

    void createNotification(User recipient, InboxNotificationType type, String title, String body, UUID referenceId);
}
