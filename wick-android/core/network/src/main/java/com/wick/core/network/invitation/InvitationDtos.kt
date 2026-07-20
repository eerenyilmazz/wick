package com.wick.core.network.invitation

import kotlinx.serialization.Serializable

@Serializable
data class SendInvitationRequestDto(
    val matchId: String,
    val useToken: Boolean = false,
)

@Serializable
data class InvitationResponseDto(
    val id: String,
    val matchId: String,
    val senderId: String,
    val status: String,
    val createdAt: String,
)
