package com.wick.core.data.notification

import com.wick.core.data.db.NotificationDao
import com.wick.core.data.db.toDomain
import com.wick.core.data.db.toEntity
import com.wick.core.model.AppResult
import com.wick.core.model.InboxNotification
import com.wick.core.model.UiError
import com.wick.core.network.notification.InboxNotificationApi
import com.wick.core.network.notification.InboxNotificationDto
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class InboxNotificationRepositoryImpl @Inject constructor(
    private val api: InboxNotificationApi,
    private val notificationDao: NotificationDao,
) : InboxNotificationRepository {

    override suspend fun getInbox(page: Int, size: Int): AppResult<List<InboxNotification>> =
        runCatching { api.getInbox(page = page, size = size).content.map { it.toDomain() } }
            .fold(
                onSuccess = { items ->
                    if (page == 0) notificationDao.deleteAll()
                    notificationDao.upsertAll(items.map { it.toEntity() })
                    AppResult.Success(items)
                },
                onFailure = {
                    val cached = notificationDao.getPage(limit = size, offset = page * size)
                    if (cached.isNotEmpty()) AppResult.Success(cached.map { it.toDomain() })
                    else it.toResult()
                },
            )

    override suspend fun markAsRead(id: String) = runCatching { notificationDao.markAsRead(id) }.getOrDefault(Unit)

    override suspend fun getUnreadCount(): Int = runCatching { notificationDao.countUnread() }.getOrDefault(0)

    override fun observeUnreadCount() = notificationDao.observeUnreadCount()

    override suspend fun markAllRead(): AppResult<Unit> =
        runCatching { api.markAllRead() }
            .fold(
                onSuccess = {
                    notificationDao.markAllRead()
                    AppResult.Success(Unit)
                },
                onFailure = { it.toResult() },
            )

    private fun InboxNotificationDto.toDomain() = InboxNotification(
        id = id,
        type = type,
        title = title,
        body = body,
        read = read,
        referenceId = referenceId,
        createdAt = createdAt,
    )

    private fun <T> Throwable.toResult(): AppResult<T> = when (this) {
        is HttpException -> AppResult.Failure(if (code() == 401) UiError.Unauthorized else UiError.Unknown)
        is IOException -> AppResult.Failure(UiError.NoConnection)
        else -> throw this
    }
}
