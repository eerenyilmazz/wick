package com.wick.core.model

data class MatchSummary(
    val id: String,
    val otherUser: User,
    val createdAt: String,
    val latestInvitation: Invitation? = null,
    val sessionStatus: String? = null,
    val sessionEndsAt: String? = null,
)
