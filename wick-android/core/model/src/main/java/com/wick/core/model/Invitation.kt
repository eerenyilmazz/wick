package com.wick.core.model

enum class InvitationStatus {
    PENDING, ACCEPTED, DECLINED, CANCELLED
}

data class Invitation(
    val id: String,
    val matchId: String,
    val senderId: String,
    val status: InvitationStatus,
    val createdAt: String,
)
