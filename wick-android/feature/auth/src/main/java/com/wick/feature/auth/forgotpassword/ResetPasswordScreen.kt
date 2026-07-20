package com.wick.feature.auth.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.model.UiError
import com.wick.core.designsystem.component.WickLogoMark
import com.wick.core.designsystem.component.WickPasswordField
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.auth.R

@Composable
fun ResetPasswordRoute(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ResetPasswordEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ResetPasswordScreen(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
private fun ResetPasswordScreen(
    uiState: ResetPasswordUiState,
    onAction: (ResetPasswordAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        WickTopBar(onBackClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.reset_password_title),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
            )
            Text(
                text = stringResource(R.string.reset_password_subtitle),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
            )

            Text(
                text = stringResource(R.string.reset_password_new_label),
                style = WickTextStyles.Label,
                color = WickColors.TextSecondary,
            )
            WickPasswordField(
                value = uiState.newPassword,
                onValueChange = { onAction(ResetPasswordAction.NewPasswordChanged(it)) },
                placeholder = stringResource(R.string.reset_password_new_placeholder),
            )

            Text(
                text = stringResource(R.string.reset_password_confirm_label),
                style = WickTextStyles.Label,
                color = WickColors.TextSecondary,
            )
            WickPasswordField(
                value = uiState.confirmPassword,
                onValueChange = { onAction(ResetPasswordAction.ConfirmPasswordChanged(it)) },
                placeholder = stringResource(R.string.reset_password_confirm_placeholder),
                isError = !uiState.doPasswordsMatch,
            )

            if (!uiState.doPasswordsMatch) {
                WickInlineWarning(text = stringResource(R.string.reset_password_mismatch))
            }

            uiState.error?.let {
                WickInlineWarning(text = it.toDisplayMessage())
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                WickLogoMark(modifier = Modifier.alpha(0.15f))
            }
        }

        ResetPasswordSubmitButton(
            enabled = uiState.canSubmit,
            loading = uiState.isLoading,
            onClick = { onAction(ResetPasswordAction.Submit) },
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.NoConnection -> stringResource(R.string.reset_password_error_no_connection)
    UiError.InvalidLink -> stringResource(R.string.reset_password_error_invalid_link)
    else -> stringResource(R.string.reset_password_error_unknown)
}

@Composable
private fun ResetPasswordSubmitButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = WickShapes.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = WickColors.Primary,
            contentColor = WickColors.OnPrimary,
            disabledContainerColor = WickColors.Primary.copy(alpha = 0.4f),
            disabledContentColor = WickColors.OnPrimary.copy(alpha = 0.6f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = WickShapes.Button,
                        ambientColor = WickColors.Primary,
                        spotColor = WickColors.Primary,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Text(text = stringResource(R.string.reset_password_submit_button), style = WickTextStyles.ButtonText)
        if (loading) {
            CircularProgressIndicator(
                color = WickColors.OnPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.padding(start = 8.dp).then(Modifier.alpha(1f)),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
