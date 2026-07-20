package com.wick.feature.auth.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
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
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.auth.R

@Composable
fun LocationPermissionScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
    onClose: () -> Unit = onSkip,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WickColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.location_permission_logo),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.Primary,
                modifier = Modifier.padding(start = 16.dp),
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.location_permission_close),
                    tint = WickColors.TextSecondary,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(WickShapes.Card)
                    .background(WickColors.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(WickShapes.Chip)
                        .background(WickColors.TextSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.location_permission_avatar_placeholder),
                        style = WickTextStyles.HeadlineMedium,
                        color = WickColors.TextSecondary,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = WickColors.Primary,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                )
            }

            Spacer(modifier = Modifier.size(40.dp))

            Text(
                text = stringResource(R.string.location_permission_title),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = stringResource(R.string.location_permission_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WickButton(
                text = stringResource(R.string.location_permission_cta),
                onClick = onAllow,
                variant = WickButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.location_permission_skip),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WickShapes.Chip)
                    .clickable(onClick = onSkip)
                    .padding(vertical = 8.dp),
            )
        }
    }
}
