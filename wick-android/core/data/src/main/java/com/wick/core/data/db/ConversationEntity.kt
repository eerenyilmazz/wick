package com.wick.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wick.core.model.ConversationSummary

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val sessionId: String,
    val matchId: String,
    val endsAt: String,
    val otherUserId: String,
    val otherDisplayName: String,
    val otherPhotoUrl: String?,
    val lastMessage: String?,
    val cachedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "ACTIVE") val status: String = "ACTIVE",
)

fun ConversationEntity.toDomain() = ConversationSummary(
    sessionId = sessionId,
    matchId = matchId,
    endsAt = endsAt,
    otherUserId = otherUserId,
    otherDisplayName = otherDisplayName,
    otherPhotoUrl = otherPhotoUrl,
    lastMessage = lastMessage,
    status = status,
)

fun ConversationSummary.toEntity() = ConversationEntity(
    sessionId = sessionId,
    matchId = matchId,
    endsAt = endsAt,
    otherUserId = otherUserId,
    otherDisplayName = otherDisplayName,
    otherPhotoUrl = otherPhotoUrl,
    lastMessage = lastMessage,
    status = status,
)
