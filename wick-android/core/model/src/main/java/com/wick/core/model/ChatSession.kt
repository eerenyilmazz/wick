package com.wick.core.model

data class ChatSession(
    val id: String,
    val matchId: String,
    val startedAt: String,
    val endsAt: String,
    val status: String,
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val clientMessageId: String? = null,
)
