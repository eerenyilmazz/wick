package com.wick.core.model

sealed interface UiError {
    data class Message(val text: String) : UiError
    data object NoConnection : UiError
    data object Unauthorized : UiError
    data object Forbidden : UiError
    data object RateLimited : UiError
    data object Conflict : UiError
    data object Unknown : UiError
    data object SessionNotFound : UiError
    data object InvalidLink : UiError
    data object ProductNotFound : UiError
    data object InsufficientTokens : UiError
}
