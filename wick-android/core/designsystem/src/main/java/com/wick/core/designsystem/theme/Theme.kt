package com.wick.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable

private val WickColorScheme = darkColorScheme(
    primary = WickColors.Primary,
    onPrimary = WickColors.OnPrimary,
    background = WickColors.Background,
    onBackground = WickColors.TextPrimary,
    surface = WickColors.Surface,
    onSurface = WickColors.TextPrimary,
    surfaceVariant = WickColors.SurfaceElevated,
    onSurfaceVariant = WickColors.TextSecondary,
    error = WickColors.Urgency,
    onError = WickColors.TextPrimary,
)

@Composable
fun WickTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalWickSpacing provides WickSpacing()) {
        MaterialTheme(
            colorScheme = WickColorScheme,
            typography = WickTypography,
            shapes = WickMaterialShapes,
            content = content,
        )
    }
}
