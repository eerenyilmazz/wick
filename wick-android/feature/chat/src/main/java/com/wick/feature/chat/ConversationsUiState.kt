package com.wick.feature.chat

import androidx.compose.runtime.Immutable
import com.wick.core.model.ConversationSummary
import com.wick.core.model.UiError

@Immutable
data class ConversationsUiState(
    val isLoading: Boolean = true,
    val conversations: List<ConversationSummary> = emptyList(),
    val error: UiError? = null,
)
