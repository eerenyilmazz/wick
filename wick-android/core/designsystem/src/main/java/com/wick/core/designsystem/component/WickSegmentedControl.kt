package com.wick.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

data class WickSegment<T>(val value: T, val label: String)

@Composable
fun <T> WickSegmentedControl(
    segments: List<WickSegment<T>>,
    selected: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(WickShapes.IconBox)
            .background(WickColors.Surface)
            .padding(4.dp),
    ) {
        segments.forEach { segment ->
            val isSelected = segment.value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(WickShapes.Chip)
                    .background(if (isSelected) WickColors.Primary else WickColors.Surface)
                    .clickable(onClick = { onSelected(segment.value) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = segment.label,
                    style = WickTextStyles.Label,
                    color = if (isSelected) WickColors.OnPrimary else WickColors.TextSecondary,
                )
            }
        }
    }
}
