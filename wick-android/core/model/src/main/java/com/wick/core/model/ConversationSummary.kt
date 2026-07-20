package com.wick.core.model

data class ConversationSummary(
    val sessionId: String,
    val matchId: String,
    val endsAt: String,
    val otherUserId: String,
    val otherDisplayName: String,
    val otherPhotoUrl: String?,
    val lastMessage: String?,
    val status: String = "ACTIVE",
)
