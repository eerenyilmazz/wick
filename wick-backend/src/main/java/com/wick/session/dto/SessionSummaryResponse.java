package com.wick.session.dto;

import com.wick.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record SessionSummaryResponse(
        UUID id,
        UUID matchId,
        Instant endsAt,
        UserResponse otherUser,
        String lastMessage,
        String status
) {
}
