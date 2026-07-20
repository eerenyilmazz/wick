package com.wick.session.listener;

import com.wick.session.service.SessionService;
import com.wick.session.service.impl.RedisSessionTimerGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class SessionExpirationListener extends KeyExpirationEventMessageListener {

    private final SessionService sessionService;

    public SessionExpirationListener(RedisMessageListenerContainer listenerContainer, SessionService sessionService) {
        super(listenerContainer);
        this.sessionService = sessionService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
        if (!expiredKey.startsWith(RedisSessionTimerGateway.KEY_PREFIX)) {
            return;
        }
        UUID sessionId = UUID.fromString(expiredKey.substring(RedisSessionTimerGateway.KEY_PREFIX.length()));
        try {
            sessionService.lock(sessionId);
        } catch (Exception e) {
            log.error("Failed to lock expired session {}", sessionId, e);
        }
    }
}
