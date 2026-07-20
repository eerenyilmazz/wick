package com.wick.eventlog.service.impl;

import com.wick.eventlog.domain.EventLog;
import com.wick.eventlog.domain.EventType;
import com.wick.eventlog.service.EventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventLogServiceImpl implements EventLogService {

    private final EventLogWriter eventLogWriter;

    @Override
    public void log(EventType eventType, UUID userId, String detail) {
        try {
            EventLog eventLog = new EventLog();
            eventLog.setEventType(eventType);
            eventLog.setUserId(userId);
            eventLog.setDetail(detail);
            eventLogWriter.write(eventLog);
        } catch (Exception writeFailure) {
            log.error("Failed to write event log: type={} userId={}", eventType, userId, writeFailure);
        }
    }
}
