package com.wick.feature.chat.navigation

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoute(
    val matchId: String,
    val otherUserId: String,
    val otherDisplayName: String,
    val otherPhotoUrl: String?,
)
