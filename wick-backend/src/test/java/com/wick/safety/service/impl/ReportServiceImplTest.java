package com.wick.safety.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.safety.domain.Report;
import com.wick.safety.domain.ReportReason;
import com.wick.safety.repository.ReportRepository;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserService userService;

    private ReportServiceImpl reportService;

    private User reporter;
    private User reported;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(reportRepository, userService);
        reporter = TestEntities.user("reporter@example.com", "hash", "Reporter");
        reported = TestEntities.user("reported@example.com", "hash", "Reported");
        ReflectionTestUtils.setField(reporter, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(reported, "id", UUID.randomUUID());
    }

    @Test
    void report_throwsBadRequest_whenReportingSelf() {
        assertThatThrownBy(() -> reportService.report(reporter.getId(), reporter.getId(), ReportReason.SPAM, "details"))
                .isInstanceOf(ApiException.class);

        verify(reportRepository, never()).save(any());
    }

    @Test
    void report_savesReport_whenValid() {
        when(userService.getById(reporter.getId())).thenReturn(reporter);
        when(userService.getById(reported.getId())).thenReturn(reported);

        reportService.report(reporter.getId(), reported.getId(), ReportReason.HARASSMENT, "details");

        verify(reportRepository).save(any(Report.class));
    }
}
