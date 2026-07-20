package com.wick.invitation.service;

import java.util.UUID;

public interface InvitationRateLimiter {

    void consumeOrThrow(UUID userId);
}
