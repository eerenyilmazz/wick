package com.wick.session.service;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.eventlog.service.EventLogService;
import com.wick.match.domain.Match;
import com.wick.match.service.MatchService;
import com.wick.notification.service.NotificationService;
import com.wick.session.config.SessionProperties;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import com.wick.session.service.impl.SessionServiceImpl;
import com.wick.token.config.TokenProperties;
import com.wick.token.domain.TokenReason;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private SessionTimerGateway sessionTimerGateway;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TokenService tokenService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventLogService eventLogService;

    private SessionServiceImpl sessionService;

    private User userA;
    private User userB;
    private Match match;

    @BeforeEach
    void setUp() {
        SessionProperties sessionProperties = new SessionProperties();
        sessionProperties.setFreeDurationMinutes(20);
        sessionProperties.setPremiumDurationMinutes(40);
        sessionProperties.setExtensionMinutes(10);

        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setSessionExtensionCost(1);

        sessionService = new SessionServiceImpl(sessionRepository, matchService, sessionTimerGateway,
                messagingTemplate, tokenService, sessionProperties, tokenProperties, notificationService, eventLogService);

        userA = TestEntities.user("a@example.com", "hash", "A");
        userB = TestEntities.user("b@example.com", "hash", "B");
        ReflectionTestUtils.setField(userA, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(userB, "id", UUID.randomUUID());
        match = TestEntities.match(userA, userB);
        ReflectionTestUtils.setField(match, "id", UUID.randomUUID());
    }

    @Test
    void start_usesFreeDuration_whenInitiatorIsNotPremium() {
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        Session session = sessionService.start(match, userA);

        assertThat(session.getEndsAt()).isAfter(session.getStartedAt().plusSeconds(19 * 60));
        assertThat(session.getEndsAt()).isBefore(session.getStartedAt().plusSeconds(21 * 60));
        verify(sessionTimerGateway).startTimer(session.getId(), Duration.ofMinutes(20));
    }

    @Test
    void start_usesPremiumDuration_whenInitiatorIsPremium() {
        userA.setPremium(true);
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> inv.getArgument(0));

        sessionService.start(match, userA);

        verify(sessionTimerGateway).startTimer(any(), eq(Duration.ofMinutes(40)));
    }

    @Test
    void lock_skipsSideEffects_whenSessionWasAlreadyLocked() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        session.setStatus(SessionStatus.LOCKED);
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.lockIfActive(session.getId())).thenReturn(0);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        sessionService.lock(session.getId());

        verify(sessionTimerGateway, never()).cancelTimer(any());
        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void lock_cancelsTimerAndBroadcasts_whenSessionTransitionsFromActive() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        session.setStatus(SessionStatus.LOCKED);
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.lockIfActive(session.getId())).thenReturn(1);
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        sessionService.lock(session.getId());

        verify(sessionTimerGateway).cancelTimer(session.getId());
        verify(messagingTemplate).convertAndSend(eq("/topic/sessions/" + session.getId() + "/status"), any(Object.class));
        verify(notificationService).notifySessionLocked(userA, session.getId());
        verify(notificationService).notifySessionLocked(userB, session.getId());
    }

    @Test
    void extend_spendsTokenAndPushesEndsAtForward() {
        Instant originalEndsAt = Instant.now().plusSeconds(60);
        Session session = TestEntities.session(match, Instant.now(), originalEndsAt);
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(matchService.involves(match, userA.getId())).thenReturn(true);
        when(sessionRepository.extendIfActive(session.getId(), 10)).thenAnswer(inv -> {
            session.setEndsAt(session.getEndsAt().plus(Duration.ofMinutes(10)));
            return 1;
        });

        Session result = sessionService.extend(session.getId(), userA.getId());

        verify(tokenService).spend(userA.getId(), 1, TokenReason.SESSION_EXTENSION);
        assertThat(result.getEndsAt()).isEqualTo(originalEndsAt.plus(Duration.ofMinutes(10)));
        verify(sessionTimerGateway).startTimer(eq(session.getId()), any(Duration.class));
    }

    @Test
    void extend_throwsConflict_whenAtomicUpdateAffectsNoRows() {
        Instant originalEndsAt = Instant.now().plusSeconds(60);
        Session session = TestEntities.session(match, Instant.now(), originalEndsAt);
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(matchService.involves(match, userA.getId())).thenReturn(true);
        when(sessionRepository.extendIfActive(session.getId(), 10)).thenReturn(0);

        assertThatThrownBy(() -> sessionService.extend(session.getId(), userA.getId()))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void extend_throwsConflict_whenSessionIsLocked() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        session.setStatus(SessionStatus.LOCKED);
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.extend(session.getId(), userA.getId()))
                .isInstanceOf(ApiException.class);
    }
}
