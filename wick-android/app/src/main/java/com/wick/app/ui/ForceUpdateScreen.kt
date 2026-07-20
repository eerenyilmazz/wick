package com.wick.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wick.app.BuildConfig
import com.wick.app.R
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun ForceUpdateScreen(onUpdate: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WickColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.app_name),
            style = WickTextStyles.HeadlineLarge,
            color = WickColors.Primary,
            modifier = Modifier.padding(bottom = 40.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WickShapes.Card)
                .background(WickColors.Surface)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(WickShapes.Card)
                    .background(WickColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdateAlt,
                    contentDescription = null,
                    tint = WickColors.OnPrimary,
                    modifier = Modifier.size(32.dp),
                )
            }

            Text(
                text = stringResource(R.string.force_update_title),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.force_update_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        WickButton(
            text = stringResource(R.string.force_update_cta) + " →",
            onClick = onUpdate,
            variant = WickButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = stringResource(R.string.force_update_version, BuildConfig.VERSION_NAME),
            style = WickTextStyles.BodySmall,
            color = WickColors.TextMuted,
        )

        Spacer(modifier = Modifier.size(32.dp))
    }
}
