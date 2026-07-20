package com.wick.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wick.core.model.InboxNotification

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String?,
    val read: Boolean,
    val referenceId: String?,
    val createdAt: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun NotificationEntity.toDomain() = InboxNotification(
    id = id,
    type = type,
    title = title,
    body = body,
    read = read,
    referenceId = referenceId,
    createdAt = createdAt,
)

fun InboxNotification.toEntity() = NotificationEntity(
    id = id,
    type = type,
    title = title,
    body = body,
    read = read,
    referenceId = referenceId,
    createdAt = createdAt,
)
