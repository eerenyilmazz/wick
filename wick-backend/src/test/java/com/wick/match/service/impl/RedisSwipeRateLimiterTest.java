package com.wick.match.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.match.config.MatchProperties;
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
class RedisSwipeRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private MatchProperties matchProperties;
    private RedisSwipeRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        matchProperties = new MatchProperties();
        matchProperties.setDailySwipeLimit(200);
        matchProperties.setSwipeLimitWindowHours(24);
        rateLimiter = new RedisSwipeRateLimiter(redisTemplate, matchProperties);
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
        when(valueOperations.increment(anyString())).thenReturn(201L);

        assertThatThrownBy(() -> rateLimiter.consumeOrThrow(userId))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void consumeOrThrow_doesNotThrow_whenCountIsWithinDailyLimit() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.increment(anyString())).thenReturn(200L);

        rateLimiter.consumeOrThrow(userId);
    }
}
