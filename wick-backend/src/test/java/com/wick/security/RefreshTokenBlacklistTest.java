package com.wick.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenBlacklistTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenBlacklist refreshTokenBlacklist;

    @BeforeEach
    void setUp() {
        refreshTokenBlacklist = new RefreshTokenBlacklist(redisTemplate);
    }

    @Test
    void blacklist_storesKeyWithRemainingTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        refreshTokenBlacklist.blacklist("jti-1", Duration.ofMinutes(5));

        verify(valueOperations).set("refresh-blacklisted:jti-1", "1", Duration.ofMinutes(5));
    }

    @Test
    void blacklist_doesNothing_whenTtlIsZeroOrNegative() {
        refreshTokenBlacklist.blacklist("jti-1", Duration.ZERO);
        refreshTokenBlacklist.blacklist("jti-2", Duration.ofSeconds(-1));

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isBlacklisted_returnsTrue_whenKeyExists() {
        when(redisTemplate.hasKey("refresh-blacklisted:jti-1")).thenReturn(true);

        assertThat(refreshTokenBlacklist.isBlacklisted("jti-1")).isTrue();
    }

    @Test
    void isBlacklisted_returnsFalse_whenKeyMissing() {
        when(redisTemplate.hasKey("refresh-blacklisted:jti-1")).thenReturn(false);

        assertThat(refreshTokenBlacklist.isBlacklisted("jti-1")).isFalse();
    }
}
