package com.wick.user.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.user.config.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAuthRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthProperties authProperties;
    private RedisAuthRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authProperties.setLoginAttemptLimit(10);
        authProperties.setLoginLimitWindowMinutes(15);
        authProperties.setRegisterAttemptLimit(5);
        authProperties.setRegisterLimitWindowMinutes(60);
        rateLimiter = new RedisAuthRateLimiter(redisTemplate, authProperties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void consumeLoginAttemptOrThrow_setsExpiry_onFirstAttempt() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimiter.consumeLoginAttemptOrThrow("eren@example.com");

        verify(redisTemplate).expire(anyString(), eq(Duration.ofMinutes(15)));
    }

    @Test
    void consumeLoginAttemptOrThrow_throws_whenLimitExceeded() {
        when(valueOperations.increment(anyString())).thenReturn(11L);

        assertThatThrownBy(() -> rateLimiter.consumeLoginAttemptOrThrow("eren@example.com"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void consumeRegisterAttemptOrThrow_throws_whenLimitExceeded() {
        when(valueOperations.increment(anyString())).thenReturn(6L);

        assertThatThrownBy(() -> rateLimiter.consumeRegisterAttemptOrThrow("eren@example.com"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void consumeRegisterAttemptOrThrow_doesNotThrow_whenWithinLimit() {
        when(valueOperations.increment(anyString())).thenReturn(5L);

        rateLimiter.consumeRegisterAttemptOrThrow("eren@example.com");
    }
}
