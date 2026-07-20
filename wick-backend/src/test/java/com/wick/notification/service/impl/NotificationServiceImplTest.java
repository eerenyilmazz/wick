package com.wick.notification.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.eventlog.service.EventLogService;
import com.wick.notification.service.InboxNotificationService;
import com.wick.notification.service.NotificationSender;
import com.wick.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private EventLogService eventLogService;

    @Mock
    private InboxNotificationService inboxNotificationService;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationSender, eventLogService, inboxNotificationService);
    }

    @Test
    void notifyInvitationReceived_sendsPush_whenRecipientHasDeviceToken() {
        User recipient = TestEntities.user("recipient@example.com", "hash", "Recipient");
        recipient.setFcmToken("device-token-1");
        User sender = TestEntities.user("sender@example.com", "hash", "Sender");

        notificationService.notifyInvitationReceived(recipient, sender);

        verify(notificationSender).send("device-token-1", "New chat invitation", "Sender wants to start a chat with you");
    }

    @Test
    void notifyInvitationReceived_doesNothing_whenRecipientHasNoDeviceToken() {
        User recipient = TestEntities.user("recipient@example.com", "hash", "Recipient");
        User sender = TestEntities.user("sender@example.com", "hash", "Sender");

        notificationService.notifyInvitationReceived(recipient, sender);

        verify(notificationSender, never()).send(any(), any(), any());
    }

    @Test
    void notifySessionLocked_sendsPush_whenUserHasDeviceToken() {
        User user = TestEntities.user("user@example.com", "hash", "User");
        user.setFcmToken("device-token-2");
        UUID sessionId = UUID.randomUUID();

        notificationService.notifySessionLocked(user, sessionId);

        verify(notificationSender).send("device-token-2", "Chat ended",
                "Your chat session has ended. Extend it with tokens to keep talking.");
    }

    @Test
    void notifySessionEndingSoon_sendsPush_whenUserHasDeviceToken() {
        User user = TestEntities.user("user@example.com", "hash", "User");
        user.setFcmToken("device-token-3");
        UUID sessionId = UUID.randomUUID();

        notificationService.notifySessionEndingSoon(user, sessionId, 120);

        verify(notificationSender).send("device-token-3", "Chat ending soon",
                "Your chat session ends in 2 minute(s).");
    }

    @Test
    void notifySessionEndingSoon_usesSeconds_whenUnderOneMinute() {
        User user = TestEntities.user("user@example.com", "hash", "User");
        user.setFcmToken("device-token-4");
        UUID sessionId = UUID.randomUUID();

        notificationService.notifySessionEndingSoon(user, sessionId, 45);

        verify(notificationSender).send("device-token-4", "Chat ending soon",
                "Your chat session ends in 45 second(s).");
    }
}
