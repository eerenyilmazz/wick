package com.wick.notification.service;

import com.wick.user.domain.User;

import java.util.UUID;

public interface NotificationService {

    void notifyMatchCreated(User userA, User userB);

    void notifySuperLike(User recipient, User sender);

    void notifyInvitationReceived(User recipient, User sender);

    void notifyInvitationAccepted(User invitationSender, User accepter);

    void notifyInvitationDeclined(User invitationSender);

    void notifySessionLocked(User user, UUID sessionId);

    void notifySessionEndingSoon(User user, UUID sessionId, long remainingSeconds);

    void notifyVerificationApproved(User user);

    void notifyVerificationRejected(User user);
}
