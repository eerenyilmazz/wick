package com.wick.session.repository;

import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByMatchIdAndStatus(UUID matchId, SessionStatus status);

    List<Session> findAllByStatus(SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.status = 'ACTIVE' AND (s.match.userOne.id = :userId OR s.match.userTwo.id = :userId)")
    List<Session> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM Session s WHERE (s.status = 'ACTIVE' OR s.status = 'LOCKED') AND (s.match.userOne.id = :userId OR s.match.userTwo.id = :userId) ORDER BY s.startedAt DESC")
    List<Session> findRecentByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM Session s JOIN FETCH s.match m JOIN FETCH m.userOne JOIN FETCH m.userTwo WHERE s.status = :status AND s.endsAt < :cutoff")
    List<Session> findAllByStatusAndEndsAtBefore(@Param("status") SessionStatus status, @Param("cutoff") Instant cutoff);

    List<Session> findAllByMatchId(UUID matchId);

    Optional<Session> findTopByMatchIdOrderByStartedAtDesc(UUID matchId);

    @Modifying(clearAutomatically = true)
    @Query("update Session s set s.status = 'LOCKED' where s.id = :id and s.status = 'ACTIVE'")
    int lockIfActive(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query(value = "update chat_sessions set ends_at = ends_at + (:extensionMinutes || ' minutes')::interval "
            + "where id = :id and status = 'ACTIVE'", nativeQuery = true)
    int extendIfActive(@Param("id") UUID id, @Param("extensionMinutes") int extensionMinutes);
}
