package com.wick.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes

@Composable
fun WickCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(WickShapes.Card)
            .background(if (elevated) WickColors.SurfaceElevated else WickColors.Surface),
    ) {
        content()
    }
}
