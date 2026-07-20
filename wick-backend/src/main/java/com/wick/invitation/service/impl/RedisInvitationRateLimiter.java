package com.wick.invitation.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.invitation.config.InvitationProperties;
import com.wick.invitation.service.InvitationRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisInvitationRateLimiter implements InvitationRateLimiter {

    private static final String KEY_PREFIX = "invite-limit:";

    private final StringRedisTemplate redisTemplate;
    private final InvitationProperties invitationProperties;

    @Override
    public void consumeOrThrow(UUID userId) {
        String key = KEY_PREFIX + userId + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofHours(invitationProperties.getLimitWindowHours()));
        }
        if (count != null && count > invitationProperties.getDailyLimit()) {
            throw new TooManyRequestsException("Daily invitation limit reached");
        }
    }
}
