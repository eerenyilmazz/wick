package com.wick.safety.controller;

import com.wick.safety.dto.ReportRequest;
import com.wick.safety.service.BlockService;
import com.wick.safety.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}")
@RequiredArgsConstructor
public class SafetyController {

    private final BlockService blockService;
    private final ReportService reportService;

    @PostMapping("/block")
    public void block(@AuthenticationPrincipal String currentUserId, @PathVariable UUID userId) {
        blockService.block(UUID.fromString(currentUserId), userId);
    }

    @DeleteMapping("/block")
    public void unblock(@AuthenticationPrincipal String currentUserId, @PathVariable UUID userId) {
        blockService.unblock(UUID.fromString(currentUserId), userId);
    }

    @PostMapping("/report")
    public void report(@AuthenticationPrincipal String currentUserId, @PathVariable UUID userId,
                        @Valid @RequestBody ReportRequest request) {
        reportService.report(UUID.fromString(currentUserId), userId, request.reason(), request.details());
    }
}
