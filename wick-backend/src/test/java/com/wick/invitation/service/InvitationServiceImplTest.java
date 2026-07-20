package com.wick.invitation.service;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ConflictException;
import com.wick.invitation.domain.Invitation;
import com.wick.invitation.domain.InvitationStatus;
import com.wick.invitation.repository.InvitationRepository;
import com.wick.invitation.service.impl.InvitationServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private UserService userService;

    @Mock
    private InvitationRateLimiter invitationRateLimiter;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenService tokenService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BlockService blockService;

    private InvitationServiceImpl invitationService;

    private User sender;
    private User recipient;
    private Match match;

    @BeforeEach
    void setUp() {
        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setExtraInvitationCost(1);

        invitationService = new InvitationServiceImpl(invitationRepository, matchService, userService,
                invitationRateLimiter, sessionService, tokenService, tokenProperties, notificationService, blockService);
        sender = TestEntities.user("sender@example.com", "hash", "Sender");
        recipient = TestEntities.user("recipient@example.com", "hash", "Recipient");
        ReflectionTestUtils.setField(sender, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(recipient, "id", UUID.randomUUID());
        match = TestEntities.match(sender, recipient);
        ReflectionTestUtils.setField(match, "id", UUID.randomUUID());
    }

    @Test
    void send_throwsForbidden_whenSenderNotPartOfMatch() {
        when(matchService.getById(match.getId())).thenReturn(match);
        when(matchService.involves(match, sender.getId())).thenReturn(false);

        assertThatThrownBy(() -> invitationService.send(match.getId(), sender.getId(), false))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void send_throwsConflict_whenPendingInvitationAlreadyExists() {
        when(matchService.getById(match.getId())).thenReturn(match);
        when(matchService.involves(match, sender.getId())).thenReturn(true);
        when(matchService.otherUser(match, sender.getId())).thenReturn(recipient);
        when(invitationRepository.findByMatchIdAndStatus(match.getId(), InvitationStatus.PENDING))
                .thenReturn(Optional.of(TestEntities.invitation(match, recipient)));

        assertThatThrownBy(() -> invitationService.send(match.getId(), sender.getId(), false))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void send_throwsConflict_whenActiveSessionAlreadyExistsForMatch() {
        when(matchService.getById(match.getId())).thenReturn(match);
        when(matchService.involves(match, sender.getId())).thenReturn(true);
        when(matchService.otherUser(match, sender.getId())).thenReturn(recipient);
        when(invitationRepository.findByMatchIdAndStatus(match.getId(), InvitationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(sessionService.findActiveByMatch(match.getId()))
                .thenReturn(Optional.of(TestEntities.session(match, java.time.Instant.now(), java.time.Instant.now())));

        assertThatThrownBy(() -> invitationService.send(match.getId(), sender.getId(), false))
                .isInstanceOf(ConflictException.class);
        verify(tokenService, never()).spend(any(), anyInt(), any());
        verify(invitationRateLimiter, never()).consumeOrThrow(any());
    }

    @Test
    void send_propagatesLimitException_whenDailyLimitExceeded() {
        when(matchService.getById(match.getId())).thenReturn(match);
        when(matchService.involves(match, sender.getId())).thenReturn(true);
        when(matchService.otherUser(match, sender.getId())).thenReturn(recipient);
        when(invitationRepository.findByMatchIdAndStatus(match.getId(), InvitationStatus.PENDING))
                .thenReturn(Optional.empty());
        org.mockito.Mockito.doThrow(new com.wick.common.exception.TooManyRequestsException("limit"))
                .when(invitationRateLimiter).consumeOrThrow(sender.getId());

        assertThatThrownBy(() -> invitationService.send(match.getId(), sender.getId(), false))
                .isInstanceOf(com.wick.common.exception.TooManyRequestsException.class);
    }

    @Test
    void send_createsInvitation_whenValid() {
        when(matchService.getById(match.getId())).thenReturn(match);
        when(matchService.involves(match, sender.getId())).thenReturn(true);
        when(invitationRepository.findByMatchIdAndStatus(match.getId(), InvitationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(userService.getById(sender.getId())).thenReturn(sender);
        when(matchService.otherUser(match, sender.getId())).thenReturn(recipient);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invitation result = invitationService.send(match.getId(), sender.getId(), false);

        assertThat(result.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(result.getSender()).isEqualTo(sender);
        verify(invitationRateLimiter).consumeOrThrow(sender.getId());
        verify(tokenService, never()).spend(any(), anyInt(), any());
        verify(notificationService).notifyInvitationReceived(recipient, sender);
    }

    @Test
    void send_spendsTokenInsteadOfRateLimit_whenUseTokenIsTrue() {
        when(matchService.getById(match.getId())).thenReturn(match);
        when(matchService.involves(match, sender.getId())).thenReturn(true);
        when(matchService.otherUser(match, sender.getId())).thenReturn(recipient);
        when(invitationRepository.findByMatchIdAndStatus(match.getId(), InvitationStatus.PENDING))
                .thenReturn(Optional.empty());
        when(userService.getById(sender.getId())).thenReturn(sender);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        invitationService.send(match.getId(), sender.getId(), true);

        verify(tokenService).spend(sender.getId(), 1, TokenReason.EXTRA_INVITATION);
        verify(invitationRateLimiter, never()).consumeOrThrow(any());
    }

    @Test
    void accept_throwsForbidden_whenSenderTriesToAcceptOwnInvitation() {
        Invitation invitation = TestEntities.invitation(match, sender);
        ReflectionTestUtils.setField(invitation, "id", UUID.randomUUID());
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.accept(invitation.getId(), sender.getId()))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void accept_throwsConflict_whenAlreadyResolved() {
        Invitation invitation = TestEntities.invitation(match, sender);
        invitation.setStatus(InvitationStatus.DECLINED);
        ReflectionTestUtils.setField(invitation, "id", UUID.randomUUID());
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        when(matchService.involves(match, recipient.getId())).thenReturn(true);

        assertThatThrownBy(() -> invitationService.accept(invitation.getId(), recipient.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void accept_marksInvitationAccepted_whenValid() {
        Invitation invitation = TestEntities.invitation(match, sender);
        ReflectionTestUtils.setField(invitation, "id", UUID.randomUUID());
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        when(matchService.involves(match, recipient.getId())).thenReturn(true);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(inv -> inv.getArgument(0));

        Invitation result = invitationService.accept(invitation.getId(), recipient.getId());

        assertThat(result.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(result.getRespondedAt()).isNotNull();
    }
}
