package com.wick.core.network.notification

import kotlinx.serialization.Serializable

@Serializable
data class InboxNotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String?,
    val read: Boolean,
    val referenceId: String?,
    val createdAt: String,
)
