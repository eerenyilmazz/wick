package com.wick.config.dto;

public record AppConfigResponse(
        int minimumAge,
        int maximumAge,
        int maxPhotosPerUser,
        int dailySwipeLimit,
        int dailyInvitationLimit,
        int freeSessionDurationMinutes,
        int premiumSessionDurationMinutes,
        int sessionExtensionMinutes,
        int sessionExtensionTokenCost,
        int extraInvitationTokenCost,
        int premiumMonthlyTokenBonus,
        boolean maintenanceMode,
        String minVersion,
        int maxRetryInvitations,
        int superLikeTokenCost
) {
}
