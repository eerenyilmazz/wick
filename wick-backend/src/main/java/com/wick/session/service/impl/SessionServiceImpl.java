package com.wick.session.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.eventlog.domain.EventType;
import com.wick.eventlog.service.EventLogService;
import com.wick.match.domain.Match;
import com.wick.match.service.MatchService;
import com.wick.notification.service.NotificationService;
import com.wick.session.config.SessionProperties;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import com.wick.session.service.SessionService;
import com.wick.session.service.SessionTimerGateway;
import com.wick.token.config.TokenProperties;
import com.wick.token.domain.TokenReason;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final MatchService matchService;
    private final SessionTimerGateway sessionTimerGateway;
    private final SimpMessagingTemplate messagingTemplate;
    private final TokenService tokenService;
    private final SessionProperties sessionProperties;
    private final TokenProperties tokenProperties;
    private final NotificationService notificationService;
    private final EventLogService eventLogService;

    @Override
    @Transactional
    public Session start(Match match, User initiator) {
        int durationMinutes = initiator.isPremium()
                ? sessionProperties.getPremiumDurationMinutes()
                : sessionProperties.getFreeDurationMinutes();
        Duration duration = Duration.ofMinutes(durationMinutes);
        Instant now = Instant.now();
        Session newSession = new Session();
        newSession.setMatch(match);
        newSession.setStartedAt(now);
        newSession.setEndsAt(now.plus(duration));
        Session session = sessionRepository.save(newSession);
        sessionTimerGateway.startTimer(session.getId(), duration);
        eventLogService.log(EventType.SESSION_STARTED, session.getId(), match.getId().toString());
        return session;
    }

    @Override
    @Transactional
    public Session lock(UUID sessionId) {
        boolean wasActive = sessionRepository.lockIfActive(sessionId) == 1;
        Session session = getById(sessionId);
        if (wasActive) {
            sessionTimerGateway.cancelTimer(sessionId);
            messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/status", SessionStatus.LOCKED);
            notificationService.notifySessionLocked(session.getMatch().getUserOne(), sessionId);
            notificationService.notifySessionLocked(session.getMatch().getUserTwo(), sessionId);
            eventLogService.log(EventType.SESSION_LOCKED, sessionId, null);
        }
        return session;
    }

    @Override
    public Session getById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    @Override
    public Optional<Session> findActiveByMatch(UUID matchId) {
        return sessionRepository.findByMatchIdAndStatus(matchId, SessionStatus.ACTIVE);
    }

    @Override
    public Optional<Session> findLatestByMatch(UUID matchId) {
        return sessionRepository.findTopByMatchIdOrderByStartedAtDesc(matchId);
    }

    @Override
    @Transactional
    public Session end(UUID sessionId, UUID requesterId) {
        Session session = getById(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ApiException("Session is not active", HttpStatus.CONFLICT);
        }
        if (!matchService.involves(session.getMatch(), requesterId)) {
            throw new ApiException("You are not part of this session", HttpStatus.FORBIDDEN);
        }
        return lock(sessionId);
    }

    @Override
    public List<Session> findActiveSessions(UUID userId) {
        return sessionRepository.findActiveByUserId(userId);
    }

    @Override
    public List<Session> findRecentSessions(UUID userId) {
        return sessionRepository.findRecentByUserId(userId);
    }

    @Override
    @Transactional
    public Session extend(UUID sessionId, UUID requesterId) {
        Session session = getById(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ApiException("Session is locked, cannot extend", HttpStatus.CONFLICT);
        }
        if (!matchService.involves(session.getMatch(), requesterId)) {
            throw new ApiException("You are not part of this session", HttpStatus.FORBIDDEN);
        }

        tokenService.spend(requesterId, tokenProperties.getSessionExtensionCost(), TokenReason.SESSION_EXTENSION);

        int updated = sessionRepository.extendIfActive(sessionId, sessionProperties.getExtensionMinutes());
        if (updated == 0) {
            throw new ApiException("Session is locked, cannot extend", HttpStatus.CONFLICT);
        }
        Session saved = getById(sessionId);

        sessionTimerGateway.startTimer(sessionId, Duration.between(Instant.now(), saved.getEndsAt()));
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/extended", saved.getEndsAt().toString());
        eventLogService.log(EventType.SESSION_EXTENDED, sessionId, saved.getEndsAt().toString());
        return saved;
    }
}
