package com.wick.core.model

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: UiError) : AppResult<Nothing>
}
