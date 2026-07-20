package com.wick.session.scheduler;

import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import com.wick.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpirationFallbackJob {

    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    @Scheduled(fixedDelayString = "${app.session.fallback-check-interval-ms}")
    public void lockExpiredSessions() {
        for (Session session : sessionRepository.findAllByStatusAndEndsAtBefore(SessionStatus.ACTIVE, Instant.now())) {
            log.warn("Fallback job locking session {} that Redis failed to expire on time", session.getId());
            sessionService.lock(session.getId());
        }
    }
}
