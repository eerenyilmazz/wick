package com.wick.core.network.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferencesDto(
    val newMatches: Boolean,
    val invites: Boolean,
    val messages: Boolean,
    val reminders: Boolean,
    val promos: Boolean,
)
