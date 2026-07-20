package com.wick.core.model

data class AppConfig(
    val minimumAge: Int,
    val maximumAge: Int,
    val maxPhotosPerUser: Int,
    val sessionExtensionTokenCost: Int = 5,
    val sessionExtensionMinutes: Int = 10,
    val extraInvitationTokenCost: Int = 1,
    val maintenanceMode: Boolean = false,
    val minVersion: String = "0.0.0",
    val maxRetryInvitations: Int = 2,
    val superLikeTokenCost: Int = 1,
)
