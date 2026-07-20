package com.wick.session.dto;

import com.wick.session.domain.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID matchId,
        Instant startedAt,
        Instant endsAt,
        SessionStatus status
) {
}
