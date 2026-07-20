package com.wick.safety.dto;

import com.wick.safety.domain.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(@NotNull ReportReason reason, @Size(max = 1000) String details) {
}
