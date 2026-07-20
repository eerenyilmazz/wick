package com.wick.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.wick.core.designsystem.theme.WickMotion
import kotlinx.coroutines.launch

class WickSwipeableCardState internal constructor() {
    val offsetX = Animatable(0f)
    var widthPx by mutableFloatStateOf(1f)

    val offsetFraction: Float
        get() = (offsetX.value / widthPx).coerceIn(-1f, 1f)

    suspend fun swipeRight() {
        offsetX.animateTo(
            targetValue = widthPx * WickMotion.SwipeExitDistanceFraction,
            animationSpec = tween(WickMotion.DurationSwipeExit, easing = WickMotion.EasingStandard),
        )
    }

    suspend fun swipeLeft() {
        offsetX.animateTo(
            targetValue = -widthPx * WickMotion.SwipeExitDistanceFraction,
            animationSpec = tween(WickMotion.DurationSwipeExit, easing = WickMotion.EasingStandard),
        )
    }

    suspend fun snapBack() {
        offsetX.animateTo(0f, tween(WickMotion.DurationStandard, easing = WickMotion.EasingStandard))
    }

    suspend fun reset() {
        offsetX.snapTo(0f)
    }
}

@Composable
fun rememberWickSwipeableCardState(key: Any? = Unit): WickSwipeableCardState =
    remember(key) { WickSwipeableCardState() }

/**
 * Draggable card that animates off-screen past [WickMotion.SwipeThresholdFraction] and snaps
 * back otherwise. [state] is shared with external triggers (e.g. action buttons) so both
 * gesture- and button-driven swipes go through the same animation.
 */
@Composable
fun WickSwipeableCard(
    state: WickSwipeableCardState,
    onSwipedLeft: () -> Unit,
    onSwipedRight: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.(offsetFraction: Float) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .onSizeChanged { state.widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .graphicsLayer {
                translationX = state.offsetX.value
                rotationZ = state.offsetFraction * WickMotion.SwipeRotationDegrees
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                state.offsetFraction > WickMotion.SwipeThresholdFraction -> {
                                    state.swipeRight()
                                    onSwipedRight()
                                }

                                state.offsetFraction < -WickMotion.SwipeThresholdFraction -> {
                                    state.swipeLeft()
                                    onSwipedLeft()
                                }

                                else -> state.snapBack()
                            }
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch { state.offsetX.snapTo(state.offsetX.value + dragAmount) }
                }
            },
    ) {
        content(state.offsetFraction)
    }
}
