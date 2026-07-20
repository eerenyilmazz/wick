package com.wick.user.dto;

import java.time.Instant;
import java.util.UUID;

public record PendingVerificationResponse(
        UUID userId,
        String displayName,
        String email,
        Instant submittedAt,
        String photoUrl
) {
}
