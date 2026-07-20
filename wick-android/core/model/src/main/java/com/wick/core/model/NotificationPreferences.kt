package com.wick.core.model

data class NotificationPreferences(
    val newMatches: Boolean,
    val invites: Boolean,
    val messages: Boolean,
    val reminders: Boolean,
    val promos: Boolean,
)
