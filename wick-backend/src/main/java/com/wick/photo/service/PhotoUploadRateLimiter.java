package com.wick.photo.service;

import java.util.UUID;

public interface PhotoUploadRateLimiter {

    void consumeOrThrow(UUID userId);
}
