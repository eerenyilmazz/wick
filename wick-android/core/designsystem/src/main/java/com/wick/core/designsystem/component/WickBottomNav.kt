package com.wick.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

data class WickBottomNavItem(
    val icon: ImageVector,
    val contentDescription: String,
    val showBadge: Boolean = false,
)

@Composable
fun WickBottomNav(
    items: List<WickBottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(WickColors.Surface)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) WickColors.Primary else WickColors.Surface,
                animationSpec = tween(220),
                label = "navItemBackground",
            )
            val iconTint by animateColorAsState(
                targetValue = if (isSelected) WickColors.OnPrimary else WickColors.TextSecondary,
                animationSpec = tween(220),
                label = "navItemTint",
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.88f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "navItemScale",
            )

            val labelColor by animateColorAsState(
                targetValue = if (isSelected) WickColors.Primary else WickColors.TextMuted,
                animationSpec = tween(220),
                label = "navItemLabel",
            )

            val throttledSelect = rememberThrottledClick { onItemSelected(index) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.clickable(onClick = throttledSelect),
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(WickShapes.IconBox)
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            tint = iconTint,
                        )
                    }
                    if (item.showBadge) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(WickColors.Primary),
                        )
                    }
                }
                Text(
                    text = item.contentDescription,
                    style = WickTextStyles.Label,
                    color = labelColor,
                )
            }
        }
    }
}
