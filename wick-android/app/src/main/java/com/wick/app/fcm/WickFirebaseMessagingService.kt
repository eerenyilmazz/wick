package com.wick.app.fcm

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wick.app.MainActivity
import com.wick.app.R
import com.wick.app.WickApplication.Companion.CHANNEL_CHAT
import com.wick.app.WickApplication.Companion.CHANNEL_GENERAL
import com.wick.app.WickApplication.Companion.CHANNEL_MATCH
import com.wick.core.data.event.AppEventBus
import com.wick.core.model.AppEvent
import com.wick.core.network.user.UpdateDeviceTokenRequestDto
import com.wick.core.network.user.UserApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class WickFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userApi: UserApi

    @Inject
    lateinit var appEventBus: AppEventBus

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notifId = AtomicInteger(0)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            runCatching { userApi.updateDeviceToken(UpdateDeviceTokenRequestDto(token)) }
                .onFailure { Timber.w(it, "FCM token sync failed") }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"]
        val type = message.data["type"] ?: "GENERAL"

        val appEvent: AppEvent? = when (type) {
            "MATCH" -> AppEvent.MatchCreated
            "SUPER_LIKE" -> AppEvent.MatchCreated
            "INVITE" -> AppEvent.InvitationReceived
            "INVITE_DECLINED" -> AppEvent.InvitationDeclined
            "SESSION_STARTED" -> AppEvent.SessionStarted
            "SESSION_LOCKED" -> AppEvent.SessionLocked
            "MESSAGE" -> AppEvent.MessageReceived
            else -> null
        }
        appEvent?.let { appEventBus.emit(it) }

        val channel = when (type) {
            "MATCH", "SUPER_LIKE", "INVITE", "INVITE_DECLINED", "SESSION_STARTED", "SESSION_LOCKED", "VERIFICATION" -> CHANNEL_MATCH
            "MESSAGE" -> CHANNEL_CHAT
            else -> CHANNEL_GENERAL
        }

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            message.data["type"]?.let { putExtra("notification_type", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notifId.get(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(body?.let { NotificationCompat.BigTextStyle().bigText(it) })
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(notifId.getAndIncrement(), notification)
        } catch (e: SecurityException) {
            Timber.w(e, "Notification permission not granted")
        }
    }
}
