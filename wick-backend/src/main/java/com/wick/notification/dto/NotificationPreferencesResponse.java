package com.wick.notification.dto;

public record NotificationPreferencesResponse(
        boolean newMatches,
        boolean invites,
        boolean messages,
        boolean reminders,
        boolean promos
) {
}
