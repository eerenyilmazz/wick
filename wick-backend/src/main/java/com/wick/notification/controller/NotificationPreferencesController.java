package com.wick.notification.controller;

import com.wick.notification.dto.NotificationPreferencesResponse;
import com.wick.notification.dto.UpdateNotificationPreferencesRequest;
import com.wick.notification.service.NotificationPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferencesController {

    private final NotificationPreferencesService service;

    @GetMapping
    public NotificationPreferencesResponse getPreferences(@AuthenticationPrincipal String userId) {
        return service.getPreferences(UUID.fromString(userId));
    }

    @PutMapping
    public NotificationPreferencesResponse updatePreferences(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        return service.updatePreferences(UUID.fromString(userId), request);
    }
}
