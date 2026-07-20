package com.wick.user.service.impl;

import com.wick.common.exception.TooManyRequestsException;
import com.wick.user.config.AuthProperties;
import com.wick.user.service.AuthRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisAuthRateLimiter implements AuthRateLimiter {

    private static final String LOGIN_KEY_PREFIX = "login-limit:";
    private static final String REGISTER_KEY_PREFIX = "register-limit:";
    private static final String EMAIL_CHECK_KEY_PREFIX = "email_check:";

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    @Override
    public void consumeLoginAttemptOrThrow(String email) {
        consumeOrThrow(LOGIN_KEY_PREFIX + email.toLowerCase(), authProperties.getLoginAttemptLimit(),
                Duration.ofMinutes(authProperties.getLoginLimitWindowMinutes()), "Too many login attempts, please try again later");
    }

    @Override
    public void consumeRegisterAttemptOrThrow(String email) {
        consumeOrThrow(REGISTER_KEY_PREFIX + email.toLowerCase(), authProperties.getRegisterAttemptLimit(),
                Duration.ofMinutes(authProperties.getRegisterLimitWindowMinutes()), "Too many registration attempts, please try again later");
    }

    @Override
    public void consumeEmailCheckAttemptOrThrow(String email) {
        consumeOrThrow(EMAIL_CHECK_KEY_PREFIX + email.toLowerCase(), authProperties.getEmailCheckAttemptLimit(),
                Duration.ofMinutes(authProperties.getEmailCheckLimitWindowMinutes()), "Too many email check attempts, please try again later");
    }

    private void consumeOrThrow(String key, int limit, Duration window, String message) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }
        if (count != null && count > limit) {
            throw new TooManyRequestsException(message);
        }
    }
}
