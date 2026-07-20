package com.wick.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

private const val CLICK_THROTTLE_MS = 600L

@Composable
fun rememberThrottledClick(onClick: () -> Unit): () -> Unit {
    val currentOnClick = rememberUpdatedState(onClick)
    return remember {
        var lastClickMs = 0L
        {
            val now = System.currentTimeMillis()
            if (now - lastClickMs >= CLICK_THROTTLE_MS) {
                lastClickMs = now
                currentOnClick.value()
            }
        }
    }
}
