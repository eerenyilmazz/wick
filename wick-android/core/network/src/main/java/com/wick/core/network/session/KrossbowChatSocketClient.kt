package com.wick.core.network.session

import com.wick.core.model.AppResult
import com.wick.core.model.ConnectionState
import com.wick.core.model.UiError
import com.wick.core.network.auth.TokenProvider
import com.wick.core.network.di.BaseUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KrossbowChatSocketClient @Inject constructor(
    @BaseUrl private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
    private val tokenProvider: TokenProvider,
    private val json: Json,
) : ChatSocketClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val _messages = MutableSharedFlow<MessageResponseDto>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _remainingSeconds = MutableSharedFlow<Pair<String, Long>>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _typing = MutableSharedFlow<Pair<String, String>>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val connectMutex = Mutex()
    private var stompSession: StompSession? = null
    private var connectionJob: Job? = null

    private val stompClient by lazy { StompClient(OkHttpWebSocketClient(okHttpClient)) }

    private val socketUrl: String
        get() = baseUrl.replaceFirst("http", "ws").trimEnd('/') + "/ws/websocket"

    override fun observeConnectionState(): Flow<ConnectionState> = _connectionState.asStateFlow()

    override suspend fun connect(): AppResult<Unit> {
        _connectionState.value = ConnectionState.CONNECTING
        return connectMutex.withLock { openSession() }
    }

    override fun subscribeToMessages(sessionId: String): Flow<MessageResponseDto> {
        startConnectionLoop(sessionId)
        return _messages.filter { it.sessionId == sessionId }
    }

    override fun subscribeToRemainingSeconds(sessionId: String): Flow<Long> =
        _remainingSeconds.filter { it.first == sessionId }.map { it.second }

    override fun subscribeToTyping(sessionId: String): Flow<String> =
        _typing.filter { it.first == sessionId }.map { it.second }

    private fun startConnectionLoop(sessionId: String) {
        connectionJob?.cancel()
        connectionJob = scope.launch {
            var attempt = 0
            while (isActive) {
                try {
                    val sess = connectMutex.withLock {
                        stompSession ?: openSession().let {
                            stompSession ?: error("Connect returned success but session is null")
                        }
                    }
                    _connectionState.value = ConnectionState.CONNECTED
                    attempt = 0

                    coroutineScope {
                        launch {
                            sess.subscribeText("/topic/sessions/$sessionId")
                                .collect { raw -> _messages.emit(json.decodeFromString(raw)) }
                        }
                        launch {
                            sess.subscribeText("/topic/sessions/$sessionId/remaining")
                                .collect { raw -> _remainingSeconds.emit(sessionId to (raw.toLongOrNull() ?: 0L)) }
                        }
                        launch {
                            sess.subscribeText("/topic/sessions/$sessionId/typing")
                                .collect { userId -> _typing.emit(sessionId to userId) }
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.w(e, "WebSocket dropped (attempt=$attempt), reconnecting")
                    _connectionState.value = ConnectionState.RECONNECTING
                    connectMutex.withLock { stompSession = null }
                    val backoffMs = minOf(1_000L shl attempt.coerceAtMost(5), 30_000L)
                    delay(backoffMs)
                    attempt++
                }
            }
        }
    }

    private suspend fun openSession(): AppResult<Unit> {
        return try {
            val token = tokenProvider.getAccessToken()
            val headers = if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
            stompSession = stompClient.connect(socketUrl, customStompConnectHeaders = headers)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "STOMP connect failed")
            AppResult.Failure(UiError.NoConnection)
        }
    }

    override suspend fun sendMessage(sessionId: String, content: String, clientMessageId: String): AppResult<Unit> {
        val sess = stompSession ?: return AppResult.Failure(UiError.NoConnection)
        return try {
            val body = json.encodeToString(
                SendMessageRequestDto.serializer(),
                SendMessageRequestDto(content = content, clientMessageId = clientMessageId),
            )
            sess.sendText("/app/sessions/$sessionId/messages", body)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            AppResult.Failure(UiError.Unknown)
        }
    }

    override suspend fun sendTyping(sessionId: String) {
        val sess = stompSession ?: return
        runCatching { sess.sendText("/app/sessions/$sessionId/typing", "") }
    }

    override suspend fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        connectMutex.withLock {
            runCatching { stompSession?.disconnect() }
            stompSession = null
        }
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
