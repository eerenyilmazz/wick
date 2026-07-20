package com.wick.match.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.match.config.MatchProperties;
import com.wick.match.service.SwipeRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisSwipeRateLimiter implements SwipeRateLimiter {

    private static final String KEY_PREFIX = "swipe-limit:";

    private final StringRedisTemplate redisTemplate;
    private final MatchProperties matchProperties;

    @Override
    public void consumeOrThrow(UUID userId) {
        String key = KEY_PREFIX + userId + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofHours(matchProperties.getSwipeLimitWindowHours()));
        }
        if (count != null && count > matchProperties.getDailySwipeLimit()) {
            throw new TooManyRequestsException("Daily swipe limit reached");
        }
    }
}
