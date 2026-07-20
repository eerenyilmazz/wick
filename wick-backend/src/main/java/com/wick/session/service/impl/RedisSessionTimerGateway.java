package com.wick.session.service.impl;

import com.wick.session.service.SessionTimerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisSessionTimerGateway implements SessionTimerGateway {

    public static final String KEY_PREFIX = "session-timer:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void startTimer(UUID sessionId, Duration duration) {
        redisTemplate.opsForValue().set(KEY_PREFIX + sessionId, "active", duration);
    }

    @Override
    public void cancelTimer(UUID sessionId) {
        redisTemplate.delete(KEY_PREFIX + sessionId);
    }
}
