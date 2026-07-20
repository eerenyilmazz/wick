package com.wick.core.network.match

import com.wick.core.network.dto.PageResponseDto
import com.wick.core.network.dto.UserResponseDto
import com.wick.core.network.invitation.InvitationResponseDto
import kotlinx.serialization.Serializable

@Serializable
data class MatchResponseDto(
    val id: String,
    val otherUser: UserResponseDto,
    val createdAt: String,
    val latestInvitation: InvitationResponseDto? = null,
    val sessionStatus: String? = null,
    val sessionEndsAt: String? = null,
)

@Serializable
data class SwipeRequestDto(
    val targetUserId: String,
    val direction: String,
)

@Serializable
data class SwipeResponseDto(
    val matched: Boolean,
    val matchId: String? = null,
)
