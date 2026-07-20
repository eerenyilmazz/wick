package com.wick.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.R
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun WickErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
) {
    val label = retryLabel ?: stringResource(R.string.error_state_retry)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = WickColors.TextMuted,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = message,
            style = WickTextStyles.Body,
            color = WickColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        WickButton(text = label, onClick = onRetry, variant = WickButtonVariant.Secondary)
    }
}
