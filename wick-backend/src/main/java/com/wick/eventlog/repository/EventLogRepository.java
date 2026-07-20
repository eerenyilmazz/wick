package com.wick.eventlog.repository;

import com.wick.eventlog.domain.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventLogRepository extends JpaRepository<EventLog, UUID> {

    List<EventLog> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
