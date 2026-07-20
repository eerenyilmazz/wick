package com.wick.core.model

data class InboxNotification(
    val id: String,
    val type: String,
    val title: String,
    val body: String?,
    val read: Boolean,
    val referenceId: String?,
    val createdAt: String,
)
