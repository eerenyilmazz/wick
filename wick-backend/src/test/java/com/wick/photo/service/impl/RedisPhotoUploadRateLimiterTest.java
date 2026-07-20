package com.wick.photo.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.photo.config.PhotoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisPhotoUploadRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private PhotoProperties photoProperties;
    private RedisPhotoUploadRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        photoProperties = new PhotoProperties();
        photoProperties.setMaxUploadsPerDay(30);
        photoProperties.setUploadLimitWindowHours(24);
        rateLimiter = new RedisPhotoUploadRateLimiter(redisTemplate, photoProperties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void consumeOrThrow_setsExpiry_onFirstCallOfTheWindow() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimiter.consumeOrThrow(userId);

        verify(redisTemplate).expire(anyString(), eq(Duration.ofHours(24)));
    }

    @Test
    void consumeOrThrow_doesNotResetExpiry_onSubsequentCalls() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.increment(anyString())).thenReturn(2L);

        rateLimiter.consumeOrThrow(userId);

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void consumeOrThrow_throws_whenCountExceedsDailyLimit() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.increment(anyString())).thenReturn(31L);

        assertThatThrownBy(() -> rateLimiter.consumeOrThrow(userId))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void consumeOrThrow_doesNotThrow_whenCountIsWithinDailyLimit() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.increment(anyString())).thenReturn(30L);

        rateLimiter.consumeOrThrow(userId);
    }
}
