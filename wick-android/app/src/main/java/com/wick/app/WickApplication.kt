package com.wick.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class WickApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val vibrationPattern = longArrayOf(0, 250, 250, 250)
        listOf(
            NotificationChannel(CHANNEL_MATCH, getString(R.string.notif_channel_match), NotificationManager.IMPORTANCE_HIGH).also {
                it.enableVibration(true)
                it.vibrationPattern = vibrationPattern
            },
            NotificationChannel(CHANNEL_CHAT, getString(R.string.notif_channel_chat), NotificationManager.IMPORTANCE_HIGH).also {
                it.enableVibration(true)
                it.vibrationPattern = vibrationPattern
            },
            NotificationChannel(CHANNEL_GENERAL, getString(R.string.notif_channel_general), NotificationManager.IMPORTANCE_DEFAULT),
        ).forEach { manager.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_MATCH = "wick_match"
        const val CHANNEL_CHAT = "wick_chat"
        const val CHANNEL_GENERAL = "wick_general"
    }
}
