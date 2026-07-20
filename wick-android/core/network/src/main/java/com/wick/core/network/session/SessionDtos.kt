package com.wick.core.network.session

import kotlinx.serialization.Serializable

@Serializable
data class SessionResponseDto(
    val id: String,
    val matchId: String,
    val startedAt: String,
    val endsAt: String,
    val status: String,
)

@Serializable
data class MessageResponseDto(
    val id: String,
    val sessionId: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val clientMessageId: String? = null,
)

@Serializable
data class SendMessageRequestDto(val content: String, val clientMessageId: String)

@Serializable
data class ConversationSummaryDto(
    val id: String,
    val matchId: String,
    val endsAt: String,
    val otherUser: OtherUserDto,
    val lastMessage: String? = null,
    val status: String = "ACTIVE",
)

@Serializable
data class OtherUserDto(
    val id: String,
    val displayName: String,
    val photos: List<PhotoItemDto> = emptyList(),
)

@Serializable
data class PhotoItemDto(
    val url: String,
    val displayOrder: Int,
)
