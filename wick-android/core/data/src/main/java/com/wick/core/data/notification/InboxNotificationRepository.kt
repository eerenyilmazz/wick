package com.wick.core.data.notification

import com.wick.core.model.AppResult
import com.wick.core.model.InboxNotification
import kotlinx.coroutines.flow.Flow

interface InboxNotificationRepository {
    suspend fun getInbox(page: Int = 0, size: Int = 20): AppResult<List<InboxNotification>>
    suspend fun markAllRead(): AppResult<Unit>
    suspend fun markAsRead(id: String)
    suspend fun getUnreadCount(): Int
    fun observeUnreadCount(): Flow<Int>
}
