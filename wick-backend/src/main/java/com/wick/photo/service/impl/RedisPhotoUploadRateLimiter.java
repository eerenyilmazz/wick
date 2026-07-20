package com.wick.photo.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.photo.config.PhotoProperties;
import com.wick.photo.service.PhotoUploadRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisPhotoUploadRateLimiter implements PhotoUploadRateLimiter {

    private static final String KEY_PREFIX = "photo-upload-limit:";

    private final StringRedisTemplate redisTemplate;
    private final PhotoProperties photoProperties;

    @Override
    public void consumeOrThrow(UUID userId) {
        String key = KEY_PREFIX + userId + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofHours(photoProperties.getUploadLimitWindowHours()));
        }
        if (count != null && count > photoProperties.getMaxUploadsPerDay()) {
            throw new TooManyRequestsException("Daily photo upload limit reached");
        }
    }
}
