package com.wick.token.service.impl;

import com.wick.token.config.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenGrantGate {

    private static final String DAILY_LOGIN_KEY_PREFIX = "daily-login-claimed:";
    private static final String VERIFICATION_BONUS_KEY_PREFIX = "verification-bonus-claimed:";

    private final StringRedisTemplate redisTemplate;
    private final TokenProperties tokenProperties;

    public boolean tryClaimDailyLogin(UUID userId) {
        String key = DAILY_LOGIN_KEY_PREFIX + userId + ":" + LocalDate.now(ZoneOffset.UTC);
        Duration ttl = Duration.ofHours(tokenProperties.getDailyLoginClaimTtlHours());
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", ttl));
    }

    public boolean tryClaimVerificationBonus(UUID userId) {
        String key = VERIFICATION_BONUS_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1"));
    }
}
