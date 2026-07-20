package com.wick.notification.service.impl;

import com.wick.eventlog.domain.EventType;
import com.wick.eventlog.service.EventLogService;
import com.wick.notification.domain.InboxNotificationType;
import com.wick.notification.service.InboxNotificationService;
import com.wick.notification.service.NotificationSender;
import com.wick.notification.service.NotificationService;
import com.wick.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationSender notificationSender;
    private final EventLogService eventLogService;
    private final InboxNotificationService inboxNotificationService;

    @Override
    public void notifyMatchCreated(User userA, User userB) {
        String titleA = "Yeni eşleşme!";
        String bodyA = userB.getDisplayName() + " ile eşleştin";
        sendIfDeviceRegistered(userA, titleA, bodyA, "MATCH");
        inboxNotificationService.createNotification(userA, InboxNotificationType.MATCH, titleA, bodyA, userB.getId());

        String titleB = "Yeni eşleşme!";
        String bodyB = userA.getDisplayName() + " ile eşleştin";
        sendIfDeviceRegistered(userB, titleB, bodyB, "MATCH");
        inboxNotificationService.createNotification(userB, InboxNotificationType.MATCH, titleB, bodyB, userA.getId());
    }

    @Override
    public void notifySuperLike(User recipient, User sender) {
        String title = "Seni çok beğendi!";
        String body = sender.getDisplayName() + " seni süper beğendi!";
        sendIfDeviceRegistered(recipient, title, body, "SUPER_LIKE");
        inboxNotificationService.createNotification(recipient, InboxNotificationType.SUPER_LIKE, title, body, sender.getId());
    }

    @Override
    public void notifyInvitationReceived(User recipient, User sender) {
        String title = "Yeni sohbet daveti";
        String body = sender.getDisplayName() + " seninle sohbet başlatmak istiyor";
        sendIfDeviceRegistered(recipient, title, body, "INVITE");
        inboxNotificationService.createNotification(recipient, InboxNotificationType.INVITE, title, body, sender.getId());
    }

    @Override
    public void notifyInvitationAccepted(User invitationSender, User accepter) {
        String senderTitle = "Davet kabul edildi!";
        String senderBody = accepter.getDisplayName() + " davetini kabul etti. Sohbet başladı!";
        sendIfDeviceRegistered(invitationSender, senderTitle, senderBody, "SESSION_STARTED");

        String accepterTitle = "Sohbet başladı!";
        String accepterBody = invitationSender.getDisplayName() + " ile sohbetin başladı. Mesajlaşmaya başla!";
        sendIfDeviceRegistered(accepter, accepterTitle, accepterBody, "SESSION_STARTED");
    }

    @Override
    public void notifyInvitationDeclined(User invitationSender) {
        String title = "Davet reddedildi";
        String body = "Gönderdiğin davet reddedildi.";
        sendIfDeviceRegistered(invitationSender, title, body, "INVITE_DECLINED");
    }

    @Override
    public void notifySessionLocked(User user, UUID sessionId) {
        String title = "Sohbet sona erdi";
        String body = "Sohbet oturumun sona erdi. Konuşmaya devam etmek için token ile uzat.";
        sendIfDeviceRegistered(user, title, body, "SESSION_LOCKED");
        inboxNotificationService.createNotification(user, InboxNotificationType.SESSION_REMINDER, title, body, sessionId);
    }

    @Override
    public void notifySessionEndingSoon(User user, UUID sessionId, long remainingSeconds) {
        String timeLeft = remainingSeconds >= 60
                ? (remainingSeconds / 60) + " dakika"
                : remainingSeconds + " saniye";
        String title = "Sohbet bitiyor";
        String body = "Sohbet oturumun " + timeLeft + " içinde sona erecek.";
        sendIfDeviceRegistered(user, title, body, "SESSION_REMINDER");
        inboxNotificationService.createNotification(user, InboxNotificationType.SESSION_REMINDER, title, body, sessionId);
    }

    @Override
    public void notifyVerificationApproved(User user) {
        String title = "Hesabın doğrulandı!";
        String body = "Profilin artık onaylı rozeti taşıyor.";
        sendIfDeviceRegistered(user, title, body, "VERIFICATION");
        inboxNotificationService.createNotification(user, InboxNotificationType.VERIFICATION_APPROVED, title, body, user.getId());
    }

    @Override
    public void notifyVerificationRejected(User user) {
        String title = "Doğrulama tamamlanamadı";
        String body = "Selfie profilinle eşleşmedi. Tekrar dene.";
        sendIfDeviceRegistered(user, title, body, "VERIFICATION");
        inboxNotificationService.createNotification(user, InboxNotificationType.VERIFICATION_REJECTED, title, body, user.getId());
    }

    private void sendIfDeviceRegistered(User user, String title, String body, String type) {
        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            return;
        }
        notificationSender.send(user.getFcmToken(), title, body, type);
        eventLogService.log(EventType.NOTIFICATION_SENT, user.getId(), title);
    }
}
