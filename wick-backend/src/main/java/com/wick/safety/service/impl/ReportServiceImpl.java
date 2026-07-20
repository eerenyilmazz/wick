package com.wick.safety.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.safety.domain.Report;
import com.wick.safety.domain.ReportReason;
import com.wick.safety.repository.ReportRepository;
import com.wick.safety.service.ReportService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void report(UUID reporterId, UUID reportedId, ReportReason reason, String details) {
        if (reporterId.equals(reportedId)) {
            throw new ApiException("Cannot report yourself", HttpStatus.BAD_REQUEST);
        }
        User reporter = userService.getById(reporterId);
        User reported = userService.getById(reportedId);
        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        report.setDetails(details);
        reportRepository.save(report);
    }
}
