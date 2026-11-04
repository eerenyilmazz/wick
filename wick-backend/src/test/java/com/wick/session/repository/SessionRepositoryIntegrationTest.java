package com.wick.session.repository;

import com.wick.testsupport.TestEntities;

import com.wick.AbstractIntegrationTest;
import com.wick.match.domain.Match;
import com.wick.match.repository.MatchRepository;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.user.domain.User;
import com.wick.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SessionRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    private Session createActiveSession() {
        User userA = userRepository.save(TestEntities.user("session-repo-a@wick.com", "hash", "A"));
        User userB = userRepository.save(TestEntities.user("session-repo-b@wick.com", "hash", "B"));
        Match match = matchRepository.save(TestEntities.match(userA, userB));
        // PostgreSQL stores microseconds; Linux Instant.now() has nanoseconds
        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        return sessionRepository.saveAndFlush(TestEntities.session(match, now, now.plusSeconds(60)));
    }

    @Test
    @Transactional
    void extendIfActive_pushesEndsAtForwardByExactInterval_onRealPostgres() {
        Session session = createActiveSession();
        Instant originalEndsAt = session.getEndsAt();

        int updated = sessionRepository.extendIfActive(session.getId(), 10);

        assertThat(updated).isEqualTo(1);
        Session reloaded = sessionRepository.findById(session.getId()).orElseThrow();
        assertThat(reloaded.getEndsAt()).isEqualTo(originalEndsAt.plusSeconds(600));
    }

    @Test
    @Transactional
    void extendIfActive_affectsNoRows_whenSessionAlreadyLocked() {
        Session session = createActiveSession();
        sessionRepository.lockIfActive(session.getId());

        int updated = sessionRepository.extendIfActive(session.getId(), 10);

        assertThat(updated).isZero();
    }

    @Test
    @Transactional
    void lockIfActive_transitionsExactlyOnce_whenCalledTwice() {
        Session session = createActiveSession();

        int firstCall = sessionRepository.lockIfActive(session.getId());
        int secondCall = sessionRepository.lockIfActive(session.getId());

        assertThat(firstCall).isEqualTo(1);
        assertThat(secondCall).isZero();
        assertThat(sessionRepository.findById(session.getId()).orElseThrow().getStatus())
                .isEqualTo(SessionStatus.LOCKED);
    }
}
