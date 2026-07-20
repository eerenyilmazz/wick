package com.wick.safety.service;

import com.wick.safety.domain.ReportReason;

import java.util.UUID;

public interface ReportService {

    void report(UUID reporterId, UUID reportedId, ReportReason reason, String details);
}
