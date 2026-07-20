package com.wick.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

object WickMotion {
    const val DurationFast = 150
    const val DurationStandard = 250
    const val DurationEnter = 350
    const val DurationSwipeExit = 300
    val EasingStandard: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    const val SwipeExitDistanceFraction = 1.6f
    const val SwipeRotationDegrees = 12f
    const val SwipeThresholdFraction = 0.35f
    const val EnterSlideFraction = 0.08f
    const val DurationNavTransition = 300
    const val NavSlideFraction = 0.25f
}
