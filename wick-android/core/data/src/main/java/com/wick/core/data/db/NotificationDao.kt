package com.wick.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notifications: List<NotificationEntity>)

    @Query("SELECT COUNT(*) FROM notifications WHERE `read` = 0")
    suspend fun countUnread(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE `read` = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE notifications SET `read` = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET `read` = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
