package com.wick.notification.dto;

public record UpdateNotificationPreferencesRequest(
        boolean newMatches,
        boolean invites,
        boolean messages,
        boolean reminders,
        boolean promos
) {
}
