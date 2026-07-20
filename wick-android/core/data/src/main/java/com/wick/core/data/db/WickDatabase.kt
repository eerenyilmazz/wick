package com.wick.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, NotificationEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class WickDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun notificationDao(): NotificationDao
}
