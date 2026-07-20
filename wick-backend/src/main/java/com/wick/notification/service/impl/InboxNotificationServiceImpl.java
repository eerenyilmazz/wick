package com.wick.notification.service.impl;

import com.wick.common.dto.PageResponse;
import com.wick.notification.domain.InboxNotification;
import com.wick.notification.domain.InboxNotificationType;
import com.wick.notification.dto.InboxNotificationResponse;
import com.wick.notification.repository.InboxNotificationRepository;
import com.wick.notification.service.InboxNotificationService;
import com.wick.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboxNotificationServiceImpl implements InboxNotificationService {

    private final InboxNotificationRepository repository;

    @Override
    public PageResponse<InboxNotificationResponse> getInbox(UUID userId, Pageable pageable) {
        return PageResponse.from(repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(n -> new InboxNotificationResponse(
                        n.getId(),
                        n.getType(),
                        n.getTitle(),
                        n.getBody(),
                        n.isRead(),
                        n.getReferenceId(),
                        n.getCreatedAt()
                )));
    }

    @Override
    @Transactional
    public void markAllRead(UUID userId) {
        repository.markAllReadByUserId(userId);
    }

    @Override
    @Transactional
    public void createNotification(User recipient, InboxNotificationType type, String title, String body, UUID referenceId) {
        InboxNotification notification = new InboxNotification();
        notification.setUser(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setReferenceId(referenceId);
        repository.save(notification);
    }
}
