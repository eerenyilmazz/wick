package com.wick.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes

@Composable
fun shimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer_alpha",
    )
    return alpha
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 14.dp,
) {
    val alpha = shimmerAlpha()
    val m = if (width != null) modifier.width(width).height(height) else modifier.fillMaxWidth().height(height)
    Box(
        modifier = m
            .alpha(alpha)
            .clip(WickShapes.Chip)
            .background(WickColors.Surface),
    )
}

@Composable
fun ShimmerListRow(modifier: Modifier = Modifier) {
    val alpha = shimmerAlpha()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(WickShapes.Chip)
                .background(WickColors.Background),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(13.dp)
                    .clip(WickShapes.Chip)
                    .background(WickColors.Background),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(11.dp)
                    .clip(WickShapes.Chip)
                    .background(WickColors.Background),
            )
        }
    }
}
