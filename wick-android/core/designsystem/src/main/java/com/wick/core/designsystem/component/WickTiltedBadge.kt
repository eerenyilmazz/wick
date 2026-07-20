package com.wick.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun WickTiltedBadge(text: String, modifier: Modifier = Modifier, rotationDegrees: Float = -6f) {
    Row(
        modifier = modifier
            .graphicsLayer { rotationZ = rotationDegrees }
            .clip(WickShapes.Chip)
            .background(WickColors.Primary)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = text, style = WickTextStyles.Label, color = WickColors.OnPrimary)
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = WickColors.OnPrimary,
            modifier = Modifier.size(14.dp),
        )
    }
}
