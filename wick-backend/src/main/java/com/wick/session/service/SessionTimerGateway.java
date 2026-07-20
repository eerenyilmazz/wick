package com.wick.session.service;

import java.time.Duration;
import java.util.UUID;

public interface SessionTimerGateway {

    void startTimer(UUID sessionId, Duration duration);

    void cancelTimer(UUID sessionId);
}
