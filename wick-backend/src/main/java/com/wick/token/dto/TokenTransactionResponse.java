package com.wick.token.dto;

import com.wick.token.domain.TokenReason;

import java.time.Instant;
import java.util.UUID;

public record TokenTransactionResponse(
        UUID id,
        int amount,
        TokenReason reason,
        Instant createdAt
) {
}
