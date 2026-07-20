package com.wick.session.scheduler;

import com.wick.testsupport.TestEntities;

import com.wick.match.domain.Match;
import com.wick.notification.config.NotificationProperties;
import com.wick.notification.service.NotificationService;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import com.wick.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionEndingSoonNotifierTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private NotificationProperties notificationProperties;
    private SessionEndingSoonNotifier notifier;

    private User userA;
    private User userB;
    private Match match;

    @BeforeEach
    void setUp() {
        notificationProperties = new NotificationProperties();
        notificationProperties.setSessionEndingSoonThresholdSeconds(60);

        notifier = new SessionEndingSoonNotifier(sessionRepository, notificationService, notificationProperties, redisTemplate);

        userA = TestEntities.user("a@example.com", "hash", "A");
        userB = TestEntities.user("b@example.com", "hash", "B");
        match = TestEntities.match(userA, userB);
    }

    @Test
    void notifyEndingSoon_sendsNotification_whenWithinThresholdAndFirstWarning() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now().plusSeconds(30));
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findAllByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(session));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);

        notifier.notifyEndingSoon();

        verify(notificationService).notifySessionEndingSoon(eq(userA), eq(session.getId()), any(Long.class));
        verify(notificationService).notifySessionEndingSoon(eq(userB), eq(session.getId()), any(Long.class));
    }

    @Test
    void notifyEndingSoon_skipsNotification_whenAlreadyWarned() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now().plusSeconds(30));
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findAllByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(session));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(false);

        notifier.notifyEndingSoon();

        verify(notificationService, never()).notifySessionEndingSoon(any(), any(), any(Long.class));
    }

    @Test
    void notifyEndingSoon_skipsNotification_whenOutsideThreshold() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now().plusSeconds(600));
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findAllByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(session));

        notifier.notifyEndingSoon();

        verify(notificationService, never()).notifySessionEndingSoon(any(), any(), any(Long.class));
    }
}
