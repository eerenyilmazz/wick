package com.wick.session.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID sessionId,
        UUID senderId,
        String content,
        Instant createdAt,
        String clientMessageId
) {
}
