package com.wick.eventlog.service.impl;

import com.wick.eventlog.domain.EventLog;
import com.wick.eventlog.domain.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventLogServiceImplTest {

    @Mock
    private EventLogWriter eventLogWriter;

    private EventLogServiceImpl eventLogService;

    @BeforeEach
    void setUp() {
        eventLogService = new EventLogServiceImpl(eventLogWriter);
    }

    @Test
    void log_delegatesToWriter() {
        UUID userId = UUID.randomUUID();

        eventLogService.log(EventType.USER_REGISTERED, userId, "detail");

        verify(eventLogWriter).write(any(EventLog.class));
    }

    @Test
    void log_doesNotPropagate_whenWriterThrows() {
        doThrow(new RuntimeException("db unavailable")).when(eventLogWriter).write(any(EventLog.class));

        eventLogService.log(EventType.USER_LOGIN, UUID.randomUUID(), null);
    }
}
