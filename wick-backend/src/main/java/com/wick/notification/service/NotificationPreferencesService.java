package com.wick.notification.service;

import com.wick.notification.dto.NotificationPreferencesResponse;
import com.wick.notification.dto.UpdateNotificationPreferencesRequest;

import java.util.UUID;

public interface NotificationPreferencesService {

    NotificationPreferencesResponse getPreferences(UUID userId);

    NotificationPreferencesResponse updatePreferences(UUID userId, UpdateNotificationPreferencesRequest request);
}
