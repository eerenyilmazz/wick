package com.wick.session.scheduler;

import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SessionCountdownBroadcaster {

    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelayString = "${app.session.countdown-interval-ms}")
    public void broadcastRemainingTime() {
        Instant now = Instant.now();
        for (Session session : sessionRepository.findAllByStatus(SessionStatus.ACTIVE)) {
            long remainingSeconds = Math.max(0, Duration.between(now, session.getEndsAt()).getSeconds());
            messagingTemplate.convertAndSend("/topic/sessions/" + session.getId() + "/remaining", remainingSeconds);
        }
    }
}
