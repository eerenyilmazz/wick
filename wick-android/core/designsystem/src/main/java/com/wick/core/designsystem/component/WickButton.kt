package com.wick.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

enum class WickButtonVariant { Primary, Secondary, Danger }

@Composable
fun WickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: WickButtonVariant = WickButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val disabledContentColor = when (variant) {
        WickButtonVariant.Primary -> WickColors.OnPrimary.copy(alpha = 0.6f)
        WickButtonVariant.Secondary, WickButtonVariant.Danger -> WickColors.TextPrimary.copy(alpha = 0.6f)
    }

    val colors = when (variant) {
        WickButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = WickColors.Primary,
            contentColor = WickColors.OnPrimary,
            disabledContainerColor = WickColors.Primary.copy(alpha = 0.4f),
            disabledContentColor = disabledContentColor,
        )

        WickButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = WickColors.Surface,
            contentColor = WickColors.TextPrimary,
            disabledContainerColor = WickColors.Surface.copy(alpha = 0.4f),
            disabledContentColor = disabledContentColor,
        )

        WickButtonVariant.Danger -> ButtonDefaults.buttonColors(
            containerColor = WickColors.Urgency,
            contentColor = WickColors.TextPrimary,
            disabledContainerColor = WickColors.Urgency.copy(alpha = 0.4f),
            disabledContentColor = disabledContentColor,
        )
    }

    val throttledClick = rememberThrottledClick(onClick)
    Button(
        onClick = throttledClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !loading,
        shape = WickShapes.Button,
        colors = colors,
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = disabledContentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = text, style = WickTextStyles.ButtonText)
        }
    }
}
