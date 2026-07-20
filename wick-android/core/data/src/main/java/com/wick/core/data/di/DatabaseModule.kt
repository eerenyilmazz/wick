package com.wick.core.data.di

import android.content.Context
import androidx.room.Room
import com.wick.core.data.db.ConversationDao
import com.wick.core.data.db.NotificationDao
import com.wick.core.data.db.WickDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WickDatabase =
        Room.databaseBuilder(context, WickDatabase::class.java, "wick.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideConversationDao(db: WickDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideNotificationDao(db: WickDatabase): NotificationDao = db.notificationDao()
}
