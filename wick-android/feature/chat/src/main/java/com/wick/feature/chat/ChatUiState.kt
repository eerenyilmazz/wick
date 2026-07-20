package com.wick.feature.chat

import androidx.compose.runtime.Immutable
import com.wick.core.model.ChatMessage
import com.wick.core.model.ChatSession
import com.wick.core.model.UiError

data class PendingMessage(
    val clientId: String,
    val content: String,
    val failed: Boolean = false,
)

@Immutable
data class ChatUiState(
    val isLoading: Boolean = true,
    val otherDisplayName: String = "",
    val otherPhotoUrl: String? = null,
    val session: ChatSession? = null,
    val messages: List<ChatMessage> = emptyList(),
    val pendingMessages: List<PendingMessage> = emptyList(),
    val remainingSeconds: Long? = null,
    val currentUserId: String? = null,
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val isExtending: Boolean = false,
    val isReconnecting: Boolean = false,
    val sessionExpired: Boolean = false,
    val sessionManuallyEnded: Boolean = false,
    val showExtendSheet: Boolean = false,
    val extendSheetShown: Boolean = false,
    val extensionTokenCost: Int = 5,
    val extensionMinutes: Int = 10,
    val error: UiError? = null,
    val extendError: UiError? = null,
    val reportedSuccessfully: Boolean = false,
    val isOtherTyping: Boolean = false,
)
