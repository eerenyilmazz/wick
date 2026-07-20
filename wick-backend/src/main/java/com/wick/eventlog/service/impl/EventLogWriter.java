package com.wick.eventlog.service.impl;

import com.wick.eventlog.domain.EventLog;
import com.wick.eventlog.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class EventLogWriter {

    private final EventLogRepository eventLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(EventLog eventLog) {
        eventLogRepository.save(eventLog);
    }
}
