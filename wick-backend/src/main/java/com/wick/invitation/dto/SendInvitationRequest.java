package com.wick.invitation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendInvitationRequest(@NotNull UUID matchId, boolean useToken) {
}
