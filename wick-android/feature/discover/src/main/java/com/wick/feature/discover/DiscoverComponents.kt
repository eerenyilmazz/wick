package com.wick.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun SwipeButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glow: Boolean = false,
    badge: String? = null,
) {
    Box(
        modifier = modifier.wrapContentWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .then(
                    if (glow) {
                        Modifier.shadow(
                            elevation = 16.dp,
                            shape = WickShapes.IconBox,
                            ambientColor = containerColor,
                            spotColor = containerColor,
                        )
                    } else {
                        Modifier
                    },
                )
                .clip(WickShapes.IconBox)
                .background(containerColor)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(28.dp),
            )
        }
        if (badge != null) {
            Text(
                text = badge,
                style = WickTextStyles.BodySmall,
                color = WickColors.OnTertiary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .clip(WickShapes.Chip)
                    .background(WickColors.Tertiary)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun DiscoverEmptyState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Primary.copy(alpha = 0.12f))
                .border(2.dp, WickColors.Primary.copy(alpha = 0.3f), WickShapes.IconBox),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                tint = WickColors.Primary,
                modifier = Modifier.size(40.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.discover_empty_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.discover_empty),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        WickButton(
            text = stringResource(R.string.discover_empty_retry),
            onClick = onRetry,
            variant = WickButtonVariant.Secondary,
            modifier = Modifier.wrapContentWidth(),
        )
    }
}
