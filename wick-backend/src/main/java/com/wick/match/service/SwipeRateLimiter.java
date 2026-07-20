package com.wick.match.service;

import java.util.UUID;

public interface SwipeRateLimiter {

    void consumeOrThrow(UUID userId);
}
