package com.wick.session.scheduler;

import com.wick.testsupport.TestEntities;

import com.wick.match.domain.Match;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import com.wick.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionCountdownBroadcasterTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private SessionCountdownBroadcaster broadcaster;

    @Test
    void broadcastRemainingTime_sendsRemainingSecondsForEachActiveSession() {
        broadcaster = new SessionCountdownBroadcaster(sessionRepository, messagingTemplate);

        User userA = TestEntities.user("a@example.com", "hash", "A");
        User userB = TestEntities.user("b@example.com", "hash", "B");
        Match match = TestEntities.match(userA, userB);
        Session session = TestEntities.session(match, Instant.now(), Instant.now().plusSeconds(120));
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findAllByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(session));

        broadcaster.broadcastRemainingTime();

        ArgumentCaptor<Long> remainingCaptor = ArgumentCaptor.forClass(Long.class);
        org.mockito.Mockito.verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/sessions/" + session.getId() + "/remaining"),
                remainingCaptor.capture());
        assertThat(remainingCaptor.getValue()).isBetween(115L, 120L);
    }

    @Test
    void broadcastRemainingTime_sendsZero_whenSessionAlreadyPastEndsAt() {
        broadcaster = new SessionCountdownBroadcaster(sessionRepository, messagingTemplate);

        User userA = TestEntities.user("a@example.com", "hash", "A");
        User userB = TestEntities.user("b@example.com", "hash", "B");
        Match match = TestEntities.match(userA, userB);
        Session session = TestEntities.session(match, Instant.now().minusSeconds(60), Instant.now().minusSeconds(1));
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionRepository.findAllByStatus(SessionStatus.ACTIVE)).thenReturn(List.of(session));

        broadcaster.broadcastRemainingTime();

        org.mockito.Mockito.verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/sessions/" + session.getId() + "/remaining"),
                org.mockito.ArgumentMatchers.eq(0L));
    }
}
