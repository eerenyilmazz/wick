package com.wick.core.model

enum class SwipeDirection {
    LIKE, SUPER_LIKE, PASS
}

data class SwipeResult(
    val matched: Boolean,
    val matchId: String?,
)
