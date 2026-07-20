package com.wick.invitation.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.invitation.config.InvitationProperties;
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
class RedisInvitationRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private InvitationProperties invitationProperties;
    private RedisInvitationRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        invitationProperties = new InvitationProperties();
        invitationProperties.setDailyLimit(3);
        invitationProperties.setLimitWindowHours(24);
        rateLimiter = new RedisInvitationRateLimiter(redisTemplate, invitationProperties);
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
        when(valueOperations.increment(anyString())).thenReturn(4L);

        assertThatThrownBy(() -> rateLimiter.consumeOrThrow(userId))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void consumeOrThrow_doesNotThrow_whenCountIsWithinDailyLimit() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.increment(anyString())).thenReturn(3L);

        rateLimiter.consumeOrThrow(userId);
    }
}
