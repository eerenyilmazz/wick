package com.wick.notification.controller;

import com.wick.common.dto.PageResponse;
import com.wick.notification.dto.InboxNotificationResponse;
import com.wick.notification.service.InboxNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class InboxNotificationController {

    private final InboxNotificationService service;

    @GetMapping
    public PageResponse<InboxNotificationResponse> getInbox(@AuthenticationPrincipal String userId,
                                                             @PageableDefault(size = 20) Pageable pageable) {
        return service.getInbox(UUID.fromString(userId), pageable);
    }

    @PostMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal String userId) {
        service.markAllRead(UUID.fromString(userId));
    }
}
