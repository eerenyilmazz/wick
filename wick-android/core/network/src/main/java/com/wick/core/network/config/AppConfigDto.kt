package com.wick.core.network.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfigResponseDto(
    val minimumAge: Int,
    val maximumAge: Int,
    val maxPhotosPerUser: Int,
    val dailySwipeLimit: Int,
    val dailyInvitationLimit: Int,
    val freeSessionDurationMinutes: Int,
    val premiumSessionDurationMinutes: Int,
    val sessionExtensionMinutes: Int,
    val sessionExtensionTokenCost: Int,
    val extraInvitationTokenCost: Int,
    val premiumMonthlyTokenBonus: Int,
    val maintenanceMode: Boolean = false,
    val minVersion: String = "0.0.0",
    val maxRetryInvitations: Int = 2,
    val superLikeTokenCost: Int = 1,
)
