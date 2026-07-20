package com.wick.eventlog.service;

import com.wick.eventlog.domain.EventType;

import java.util.UUID;

public interface EventLogService {

    void log(EventType eventType, UUID userId, String detail);
}
