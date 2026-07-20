package com.wick.invitation.dto;

import com.wick.invitation.domain.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID matchId,
        UUID senderId,
        InvitationStatus status,
        Instant createdAt
) {
}
