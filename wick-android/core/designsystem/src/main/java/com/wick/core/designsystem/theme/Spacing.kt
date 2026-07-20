package com.wick.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class WickSpacing(
    val base: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 16.dp,
    val md: Dp = 24.dp,
    val lg: Dp = 32.dp,
    val xl: Dp = 48.dp,
    val screenPadding: Dp = 20.dp,
)

val LocalWickSpacing = staticCompositionLocalOf { WickSpacing() }
