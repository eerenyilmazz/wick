package com.wick.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wick.app.R
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun ConnectionErrorScreen(
    onBack: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WickColors.Background),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp, start = 8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.connection_error_back),
                tint = WickColors.Primary,
            )
        }

        Text(
            text = stringResource(R.string.app_name),
            style = WickTextStyles.HeadlineLarge,
            color = WickColors.Primary,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(WickShapes.Card)
                    .background(WickColors.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = WickColors.TextMuted,
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(modifier = Modifier.size(32.dp))

            Text(
                text = stringResource(R.string.connection_error_title),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = stringResource(R.string.connection_error_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.size(40.dp))

            WickButton(
                text = stringResource(R.string.connection_error_retry),
                onClick = onRetry,
                variant = WickButtonVariant.Primary,
            )
        }
    }
}
