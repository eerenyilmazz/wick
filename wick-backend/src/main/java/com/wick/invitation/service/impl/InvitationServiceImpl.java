package com.wick.invitation.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ConflictException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.invitation.domain.Invitation;
import com.wick.invitation.domain.InvitationStatus;
import com.wick.invitation.repository.InvitationRepository;
import com.wick.invitation.config.InvitationProperties;
import com.wick.invitation.service.InvitationRateLimiter;
import com.wick.invitation.service.InvitationService;
import com.wick.match.domain.Match;
import com.wick.match.service.MatchService;
import com.wick.notification.service.NotificationService;
import com.wick.safety.service.BlockService;
import com.wick.session.service.SessionService;
import com.wick.token.config.TokenProperties;
import com.wick.token.domain.TokenReason;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final MatchService matchService;
    private final UserService userService;
    private final InvitationRateLimiter invitationRateLimiter;
    private final SessionService sessionService;
    private final TokenService tokenService;
    private final TokenProperties tokenProperties;
    private final InvitationProperties invitationProperties;
    private final NotificationService notificationService;
    private final BlockService blockService;

    @Override
    @Transactional
    public Invitation send(UUID matchId, UUID senderId, boolean useToken) {
        Match match = matchService.getById(matchId);
        if (!matchService.involves(match, senderId)) {
            throw new ApiException("You are not part of this match", HttpStatus.FORBIDDEN);
        }
        if (blockService.isBlockedEitherDirection(senderId, matchService.otherUser(match, senderId).getId())) {
            throw new ApiException("You cannot interact with this user", HttpStatus.FORBIDDEN);
        }
        if (invitationRepository.findByMatchIdAndStatus(matchId, InvitationStatus.PENDING).isPresent()) {
            throw new ConflictException("An invitation is already pending for this match");
        }
        if (sessionService.findActiveByMatch(matchId).isPresent()) {
            throw new ConflictException("There is already an active chat session for this match");
        }

        long declinedCount = invitationRepository.countByMatchIdAndSenderIdAndStatus(matchId, senderId, InvitationStatus.DECLINED);
        if (declinedCount > 0) {
            if (declinedCount >= invitationProperties.getMaxRetryInvitations()) {
                throw new ApiException("Max retry invitations reached for this match", HttpStatus.TOO_MANY_REQUESTS);
            }
            tokenService.spend(senderId, tokenProperties.getExtraInvitationCost(), TokenReason.EXTRA_INVITATION);
        } else if (useToken) {
            tokenService.spend(senderId, tokenProperties.getExtraInvitationCost(), TokenReason.EXTRA_INVITATION);
        } else {
            invitationRateLimiter.consumeOrThrow(senderId);
        }

        User sender = userService.getById(senderId);
        Invitation newInvitation = new Invitation();
        newInvitation.setMatch(match);
        newInvitation.setSender(sender);
        Invitation invitation = invitationRepository.save(newInvitation);
        notificationService.notifyInvitationReceived(matchService.otherUser(match, senderId), sender);
        return invitation;
    }

    @Override
    @Transactional
    public Invitation accept(UUID invitationId, UUID userId) {
        Invitation invitation = getPendingForRecipient(invitationId, userId);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(Instant.now());
        Invitation accepted = invitationRepository.save(invitation);
        sessionService.start(accepted.getMatch(), accepted.getSender());
        User accepter = userService.getById(userId);
        notificationService.notifyInvitationAccepted(accepted.getSender(), accepter);
        return accepted;
    }

    @Override
    @Transactional
    public Invitation decline(UUID invitationId, UUID userId) {
        Invitation invitation = getPendingForRecipient(invitationId, userId);
        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(Instant.now());
        Invitation declined = invitationRepository.save(invitation);
        notificationService.notifyInvitationDeclined(declined.getSender());
        return declined;
    }

    @Override
    public List<Invitation> getIncoming(UUID userId) {
        return invitationRepository.findIncomingPending(userId);
    }

    @Override
    @Transactional
    public Invitation cancel(UUID invitationId, UUID userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        if (!invitation.getSender().getId().equals(userId)) {
            throw new ApiException("You can only cancel your own invitations", HttpStatus.FORBIDDEN);
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Invitation already resolved");
        }
        invitation.setStatus(InvitationStatus.CANCELLED);
        return invitationRepository.save(invitation);
    }

    @Override
    public List<Invitation> getSentByUser(UUID userId) {
        return invitationRepository.findAllBySenderId(userId);
    }

    private Invitation getPendingForRecipient(UUID invitationId, UUID userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        if (invitation.getSender().getId().equals(userId) || !matchService.involves(invitation.getMatch(), userId)) {
            throw new ApiException("You cannot respond to this invitation", HttpStatus.FORBIDDEN);
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Invitation already resolved");
        }
        return invitation;
    }
}
