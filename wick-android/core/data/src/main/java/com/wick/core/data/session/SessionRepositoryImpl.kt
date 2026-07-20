package com.wick.core.data.session

import com.wick.core.data.db.ConversationDao
import com.wick.core.data.db.toDomain
import com.wick.core.data.db.toEntity
import com.wick.core.model.AppResult
import com.wick.core.model.ChatMessage
import com.wick.core.model.ChatSession
import com.wick.core.model.ConnectionState
import com.wick.core.model.ConversationSummary
import com.wick.core.model.UiError
import com.wick.core.network.session.ChatSocketClient
import com.wick.core.network.session.MessageResponseDto
import com.wick.core.network.session.SessionApi
import com.wick.core.network.session.SessionResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionApi: SessionApi,
    private val chatSocketClient: ChatSocketClient,
    private val conversationDao: ConversationDao,
) : SessionRepository {

    override suspend fun listConversations(): AppResult<List<ConversationSummary>> {
        val result = refreshConversations()
        if (result is AppResult.Failure) {
            val cached = conversationDao.getAll()
            if (cached.isNotEmpty()) return AppResult.Success(cached.map { it.toDomain() })
        }
        return result
    }

    private suspend fun refreshConversations(): AppResult<List<ConversationSummary>> =
        runCatching { sessionApi.getMySessions() }.fold(
            onSuccess = { list ->
                val conversations = list.map { dto ->
                    ConversationSummary(
                        sessionId = dto.id,
                        matchId = dto.matchId,
                        endsAt = dto.endsAt,
                        otherUserId = dto.otherUser.id,
                        otherDisplayName = dto.otherUser.displayName,
                        otherPhotoUrl = dto.otherUser.photos.minByOrNull { it.displayOrder }?.url,
                        lastMessage = dto.lastMessage,
                        status = dto.status,
                    )
                }
                conversationDao.deleteAll()
                conversationDao.upsertAll(conversations.map { it.toEntity() })
                AppResult.Success(conversations)
            },
            onFailure = { it.toAppResult() },
        )

    override suspend fun getActiveSession(matchId: String): AppResult<ChatSession> = runCatching {
        sessionApi.getActiveForMatch(matchId)
    }.fold(onSuccess = { AppResult.Success(it.toDomain()) }, onFailure = { it.toAppResult() })

    override suspend fun getLatestSession(matchId: String): AppResult<ChatSession> = runCatching {
        sessionApi.getLatestForMatch(matchId)
    }.fold(onSuccess = { AppResult.Success(it.toDomain()) }, onFailure = { it.toAppResult() })

    override suspend fun getMessageHistory(sessionId: String): AppResult<List<ChatMessage>> = runCatching {
        sessionApi.getMessages(sessionId)
    }.fold(
        onSuccess = { messages -> AppResult.Success(messages.map { it.toDomain() }) },
        onFailure = { it.toAppResult() },
    )

    override suspend fun extendSession(sessionId: String): AppResult<ChatSession> = runCatching {
        sessionApi.extend(sessionId)
    }.fold(onSuccess = { AppResult.Success(it.toDomain()) }, onFailure = { it.toAppResult() })

    override suspend fun endSession(sessionId: String): AppResult<Unit> = runCatching {
        sessionApi.end(sessionId)
    }.fold(onSuccess = { AppResult.Success(Unit) }, onFailure = { it.toAppResult() })

    override suspend fun getMissedMessages(sessionId: String, since: String): AppResult<List<ChatMessage>> =
        runCatching { sessionApi.getMessages(sessionId, since = since) }
            .fold(
                onSuccess = { messages -> AppResult.Success(messages.map { it.toDomain() }) },
                onFailure = { it.toAppResult() },
            )

    override suspend fun connect(): AppResult<Unit> = chatSocketClient.connect()

    override fun observeConnectionState(): Flow<ConnectionState> = chatSocketClient.observeConnectionState()

    override fun observeMessages(sessionId: String): Flow<ChatMessage> =
        chatSocketClient.subscribeToMessages(sessionId).map { it.toDomain() }

    override fun observeRemainingSeconds(sessionId: String): Flow<Long> =
        chatSocketClient.subscribeToRemainingSeconds(sessionId)

    override fun observeTyping(sessionId: String): Flow<String> =
        chatSocketClient.subscribeToTyping(sessionId)

    override suspend fun sendMessage(sessionId: String, content: String, clientMessageId: String): AppResult<Unit> =
        chatSocketClient.sendMessage(sessionId, content, clientMessageId)

    override suspend fun sendTyping(sessionId: String) = chatSocketClient.sendTyping(sessionId)

    override suspend fun disconnect() = chatSocketClient.disconnect()

    private fun SessionResponseDto.toDomain() = ChatSession(
        id = id,
        matchId = matchId,
        startedAt = startedAt,
        endsAt = endsAt,
        status = status,
    )

    private fun MessageResponseDto.toDomain() = ChatMessage(
        id = id,
        sessionId = sessionId,
        senderId = senderId,
        content = content,
        createdAt = createdAt,
        clientMessageId = clientMessageId,
    )

    private fun <T> Throwable.toAppResult(): AppResult<T> = when (this) {
        is HttpException -> AppResult.Failure(
            when (code()) {
                401 -> UiError.Unauthorized
                402 -> UiError.InsufficientTokens
                403 -> UiError.Forbidden
                404 -> UiError.SessionNotFound
                409 -> UiError.Conflict
                429 -> UiError.RateLimited
                else -> UiError.Unknown
            },
        )
        is IOException -> AppResult.Failure(UiError.NoConnection)
        else -> throw this
    }
}
