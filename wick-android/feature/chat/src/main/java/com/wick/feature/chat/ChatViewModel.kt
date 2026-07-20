package com.wick.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.wick.core.data.config.ConfigRepository
import com.wick.core.data.safety.SafetyRepository
import com.wick.core.data.session.SessionRepository
import com.wick.core.data.user.ProfileRepository
import com.wick.core.model.AppResult
import com.wick.core.model.ConnectionState
import com.wick.core.model.ReportReason
import com.wick.feature.chat.navigation.ChatRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val configRepository: ConfigRepository,
    private val safetyRepository: SafetyRepository,
) : ViewModel() {

    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var countdownJob: Job? = null
    private var typingJob: Job? = null
    private var typingSendCooldown = false

    private val args = savedStateHandle.toRoute<ChatRoute>()

    private val _uiState = MutableStateFlow(
        ChatUiState(otherDisplayName = args.otherDisplayName, otherPhotoUrl = args.otherPhotoUrl),
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var hasConnectedOnce = false

    init {
        viewModelScope.launch {
            val userResult = profileRepository.getCurrentUser()
            if (userResult is AppResult.Success) {
                _uiState.value = _uiState.value.copy(currentUserId = userResult.data.id)
            }
            val configResult = configRepository.getConfig()
            if (configResult is AppResult.Success) {
                _uiState.value = _uiState.value.copy(
                    extensionTokenCost = configResult.data.sessionExtensionTokenCost,
                    extensionMinutes = configResult.data.sessionExtensionMinutes,
                )
            }
            loadSession()
        }
        viewModelScope.launch {
            sessionRepository.observeConnectionState().collect { state ->
                _uiState.value = _uiState.value.copy(isReconnecting = state == ConnectionState.RECONNECTING)
                if (state == ConnectionState.CONNECTED && hasConnectedOnce) {
                    fetchMissedMessages()
                }
                if (state == ConnectionState.CONNECTED) hasConnectedOnce = true
            }
        }
    }

    fun onDraftChanged(value: String) {
        _uiState.value = _uiState.value.copy(draftMessage = value)
        if (value.isNotEmpty()) sendTypingThrottled()
    }

    private fun sendTypingThrottled() {
        if (typingSendCooldown) return
        val sessionId = _uiState.value.session?.id ?: return
        typingSendCooldown = true
        viewModelScope.launch {
            sessionRepository.sendTyping(sessionId)
            delay(2_000)
            typingSendCooldown = false
        }
    }

    fun sendMessage() {
        val state = _uiState.value
        val sessionId = state.session?.id ?: return
        val content = state.draftMessage.trim()
        if (content.isEmpty() || state.isSending) return

        val clientMessageId = UUID.randomUUID().toString()
        val pending = PendingMessage(clientId = clientMessageId, content = content)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                draftMessage = "",
                error = null,
                pendingMessages = _uiState.value.pendingMessages + pending,
            )
            when (val result = sessionRepository.sendMessage(sessionId, content, clientMessageId)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isSending = false)
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSending = false,
                    pendingMessages = _uiState.value.pendingMessages.map {
                        if (it.clientId == clientMessageId) it.copy(failed = true) else it
                    },
                )
            }
        }
    }

    fun retrySendMessage(clientId: String) {
        val state = _uiState.value
        val sessionId = state.session?.id ?: return
        val pending = state.pendingMessages.find { it.clientId == clientId } ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                pendingMessages = _uiState.value.pendingMessages.map {
                    if (it.clientId == clientId) it.copy(failed = false) else it
                },
            )
            when (sessionRepository.sendMessage(sessionId, pending.content, clientId)) {
                is AppResult.Success -> Unit
                is AppResult.Failure -> _uiState.value = _uiState.value.copy(
                    pendingMessages = _uiState.value.pendingMessages.map {
                        if (it.clientId == clientId) it.copy(failed = true) else it
                    },
                )
            }
        }
    }

    fun dismissExtendSheet() {
        _uiState.value = _uiState.value.copy(showExtendSheet = false, extendError = null)
    }

    fun endSession() {
        val sessionId = _uiState.value.session?.id ?: return
        countdownJob?.cancel()
        countdownJob = null
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showExtendSheet = false)
            sessionRepository.endSession(sessionId)
            _uiState.value = _uiState.value.copy(sessionExpired = true, sessionManuallyEnded = true)
        }
    }

    fun extendSession() {
        val sessionId = _uiState.value.session?.id ?: return
        if (_uiState.value.isExtending) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExtending = true, showExtendSheet = false, error = null, extendError = null)
            _uiState.value = when (val result = sessionRepository.extendSession(sessionId)) {
                is AppResult.Success -> _uiState.value.copy(
                    isExtending = false,
                    session = result.data,
                    sessionExpired = false,
                )
                is AppResult.Failure -> if (result.error == com.wick.core.model.UiError.InsufficientTokens) {
                    _uiState.value.copy(isExtending = false, showExtendSheet = true, extendError = result.error)
                } else {
                    _uiState.value.copy(isExtending = false, error = result.error)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupScope.launch { sessionRepository.disconnect() }
        cleanupScope.cancel()
    }

    private suspend fun loadSession() {
        when (val result = sessionRepository.getLatestSession(args.matchId)) {
            is AppResult.Success -> {
                val session = result.data
                val isLocked = session.status == "LOCKED"
                _uiState.value = _uiState.value.copy(
                    session = session,
                    sessionExpired = isLocked,
                )
                loadHistoryAndSubscribe(session.id, subscribeToSocket = !isLocked)
            }
            is AppResult.Failure -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.error)
        }
    }

    private suspend fun loadHistoryAndSubscribe(sessionId: String, subscribeToSocket: Boolean = true) {
        when (val history = sessionRepository.getMessageHistory(sessionId)) {
            is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, messages = history.data)
            is AppResult.Failure -> _uiState.value = _uiState.value.copy(isLoading = false, error = history.error)
        }

        if (!subscribeToSocket) return

        val connectResult = sessionRepository.connect()
        if (connectResult is AppResult.Failure) {
            _uiState.value = _uiState.value.copy(error = connectResult.error)
            return
        }

        viewModelScope.launch {
            sessionRepository.observeMessages(sessionId).collect { message ->
                val state = _uiState.value
                if (state.messages.none { it.id == message.id }) {
                    _uiState.value = state.copy(
                        messages = state.messages + message,
                        pendingMessages = state.pendingMessages.filterNot {
                            it.clientId == message.clientMessageId
                        },
                    )
                }
            }
        }

        viewModelScope.launch {
            sessionRepository.observeTyping(sessionId).collect { typingUserId ->
                val currentUserId = _uiState.value.currentUserId ?: return@collect
                if (typingUserId != currentUserId) {
                    typingJob?.cancel()
                    _uiState.value = _uiState.value.copy(isOtherTyping = true)
                    typingJob = viewModelScope.launch {
                        delay(3_000)
                        _uiState.value = _uiState.value.copy(isOtherTyping = false)
                    }
                }
            }
        }

        viewModelScope.launch {
            sessionRepository.observeRemainingSeconds(sessionId).collect { seconds ->
                countdownJob?.cancel()
                applyRemainingSeconds(seconds)
                if (seconds > 0) {
                    countdownJob = viewModelScope.launch {
                        var remaining = seconds
                        while (remaining > 0) {
                            delay(1_000)
                            remaining--
                            applyRemainingSeconds(remaining)
                        }
                    }
                }
            }
        }
    }

    fun blockUser() {
        countdownJob?.cancel()
        countdownJob = null
        viewModelScope.launch {
            safetyRepository.block(args.otherUserId)
            _uiState.value = _uiState.value.copy(
                sessionExpired = true,
                sessionManuallyEnded = true,
                showExtendSheet = false,
            )
        }
    }

    fun reportUser(reason: ReportReason) {
        viewModelScope.launch {
            val result = safetyRepository.report(args.otherUserId, reason, null)
            if (result is AppResult.Success) {
                _uiState.value = _uiState.value.copy(reportedSuccessfully = true)
            }
        }
    }

    fun clearReportSuccess() {
        _uiState.value = _uiState.value.copy(reportedSuccessfully = false)
    }

    private fun applyRemainingSeconds(seconds: Long) {
        val state = _uiState.value
        if (state.sessionManuallyEnded) return
        val crossedThreshold = !state.extendSheetShown && seconds in 1..300
        _uiState.value = state.copy(
            remainingSeconds = seconds,
            sessionExpired = seconds <= 0,
            showExtendSheet = if (crossedThreshold) true else state.showExtendSheet,
            extendSheetShown = if (crossedThreshold) true else state.extendSheetShown,
        )
    }

    private fun fetchMissedMessages() {
        val sessionId = _uiState.value.session?.id ?: return
        val since = _uiState.value.messages.lastOrNull()?.createdAt ?: return

        viewModelScope.launch {
            val result = sessionRepository.getMissedMessages(sessionId, since)
            if (result is AppResult.Success) {
                val known = _uiState.value.messages.map { it.id }.toHashSet()
                val newMessages = result.data.filter { it.id !in known }
                if (newMessages.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + newMessages,
                    )
                }
            }
        }
    }
}
