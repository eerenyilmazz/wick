package com.wick.session.listener;

import com.wick.session.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionExpirationListenerTest {

    @Mock
    private RedisMessageListenerContainer listenerContainer;

    @Mock
    private SessionService sessionService;

    @Mock
    private Message message;

    private SessionExpirationListener listener;

    @BeforeEach
    void setUp() {
        listener = new SessionExpirationListener(listenerContainer, sessionService);
    }

    @Test
    void onMessage_locksSession_whenKeyIsASessionTimerKey() {
        UUID sessionId = UUID.randomUUID();
        when(message.getBody()).thenReturn(("session-timer:" + sessionId).getBytes(StandardCharsets.UTF_8));

        listener.onMessage(message, null);

        verify(sessionService).lock(sessionId);
    }

    @Test
    void onMessage_ignoresKey_whenNotASessionTimerKey() {
        when(message.getBody()).thenReturn("some-other-key:123".getBytes(StandardCharsets.UTF_8));

        listener.onMessage(message, null);

        verify(sessionService, never()).lock(any());
    }

    @Test
    void onMessage_doesNotPropagate_whenSessionServiceThrows() {
        UUID sessionId = UUID.randomUUID();
        when(message.getBody()).thenReturn(("session-timer:" + sessionId).getBytes(StandardCharsets.UTF_8));
        when(sessionService.lock(sessionId)).thenThrow(new RuntimeException("boom"));

        listener.onMessage(message, null);

        verify(sessionService).lock(sessionId);
    }
}
