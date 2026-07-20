package com.wick.match.dto;

import com.wick.match.domain.SwipeDirection;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SwipeRequest(@NotNull UUID targetUserId, @NotNull SwipeDirection direction) {
}
