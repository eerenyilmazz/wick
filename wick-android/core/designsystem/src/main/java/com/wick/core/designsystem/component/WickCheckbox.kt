package com.wick.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun WickCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = WickColors.Primary,
                uncheckedColor = WickColors.TextMuted,
                checkmarkColor = WickColors.OnPrimary,
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = WickTextStyles.BodySmall,
            color = if (enabled) WickColors.TextSecondary else WickColors.TextMuted,
        )
    }
}
