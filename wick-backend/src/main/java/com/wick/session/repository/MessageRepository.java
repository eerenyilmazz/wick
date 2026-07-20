package com.wick.session.repository;

import com.wick.session.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    List<Message> findAllBySessionIdAndCreatedAtAfterOrderByCreatedAtAsc(UUID sessionId, Instant since);

    Optional<Message> findTopBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    Optional<Message> findByClientMessageId(String clientMessageId);

    void deleteAllBySessionId(UUID sessionId);
}
