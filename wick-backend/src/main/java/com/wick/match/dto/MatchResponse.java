package com.wick.match.dto;

import com.wick.invitation.dto.InvitationResponse;
import com.wick.user.dto.UserResponse;

import java.time.Instant;
import java.util.UUID;

public record MatchResponse(UUID id, UserResponse otherUser, Instant createdAt, InvitationResponse latestInvitation, String sessionStatus, Instant sessionEndsAt) {
}
