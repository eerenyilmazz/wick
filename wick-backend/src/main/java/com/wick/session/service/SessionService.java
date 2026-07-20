package com.wick.session.service;

import com.wick.match.domain.Match;
import com.wick.session.domain.Session;
import com.wick.user.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {

    Session start(Match match, User initiator);

    Session lock(UUID sessionId);

    Session getById(UUID sessionId);

    Optional<Session> findActiveByMatch(UUID matchId);

    Optional<Session> findLatestByMatch(UUID matchId);

    Session extend(UUID sessionId, UUID requesterId);

    Session end(UUID sessionId, UUID requesterId);

    List<Session> findActiveSessions(UUID userId);

    List<Session> findRecentSessions(UUID userId);
}
