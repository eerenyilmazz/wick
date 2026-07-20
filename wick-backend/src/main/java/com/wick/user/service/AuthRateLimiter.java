package com.wick.user.service;

public interface AuthRateLimiter {

    void consumeLoginAttemptOrThrow(String email);

    void consumeRegisterAttemptOrThrow(String email);

    void consumeEmailCheckAttemptOrThrow(String email);
}
