package com.wick.feature.profile.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickPasswordField
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.profile.R

@Composable
fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WickShapes.Card)
                .background(WickColors.SurfaceElevated)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_home_logout_confirm_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
            )
            Text(
                text = stringResource(R.string.profile_home_logout_confirm_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
            )
            WickButton(
                text = stringResource(R.string.profile_home_logout_confirm_action),
                onClick = onConfirm,
            )
            WickButton(
                text = stringResource(R.string.profile_home_delete_cancel),
                onClick = onDismiss,
                variant = WickButtonVariant.Secondary,
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    password: String,
    onPasswordChanged: (String) -> Unit,
    hasError: Boolean,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(WickShapes.Card)
                .background(WickColors.SurfaceElevated)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(WickShapes.Card)
                    .background(WickColors.Urgency.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = WickColors.Urgency,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = stringResource(R.string.profile_home_delete_confirm_title),
                style = WickTextStyles.HeadlineMedium,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.profile_home_delete_confirm_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            WickPasswordField(
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = stringResource(R.string.profile_home_delete_password_placeholder),
                isError = hasError,
            )
            if (hasError) {
                WickInlineWarning(text = stringResource(R.string.profile_home_delete_password_error))
            }
            WickButton(
                text = stringResource(R.string.profile_home_delete_confirm_action),
                onClick = onConfirm,
                variant = WickButtonVariant.Danger,
                enabled = password.isNotBlank() && !isDeleting,
                loading = isDeleting,
            )
            WickButton(
                text = stringResource(R.string.profile_home_delete_cancel),
                onClick = onDismiss,
                variant = WickButtonVariant.Secondary,
                enabled = !isDeleting,
            )
        }
    }
}
