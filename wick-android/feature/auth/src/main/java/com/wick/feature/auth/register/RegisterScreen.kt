package com.wick.feature.auth.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickEnterTransition
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickLogoMark
import com.wick.core.designsystem.component.WickPasswordField
import com.wick.core.designsystem.component.WickTextField
import com.wick.core.designsystem.component.WickTiltedBadge
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.UiError
import com.wick.feature.auth.R

@Composable
fun RegisterRoute(
    onNavigateToLegalConsent: (email: String, password: String, displayName: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.NavigateToLegalConsent -> onNavigateToLegalConsent(
                    event.email,
                    event.password,
                    event.displayName,
                )
            }
        }
    }

    RegisterScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
private fun RegisterScreen(
    uiState: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        WickTopBar(onBackClick = onNavigateToLogin)

        WickEnterTransition {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                    WickLogoMark()

                    WickTiltedBadge(text = stringResource(R.string.register_welcome_badge))

                    Text(
                        text = stringResource(R.string.register_title),
                        style = WickTextStyles.HeadlineLarge,
                        color = WickColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(R.string.register_subtitle),
                        style = WickTextStyles.Body,
                        color = WickColors.TextSecondary,
                    )

                    WickTextField(
                        value = uiState.displayName,
                        onValueChange = { onAction(RegisterAction.DisplayNameChanged(it)) },
                        placeholder = stringResource(R.string.register_display_name_placeholder),
                    )

                    WickTextField(
                        value = uiState.email,
                        onValueChange = { onAction(RegisterAction.EmailChanged(it)) },
                        placeholder = stringResource(R.string.register_email_placeholder),
                        keyboardType = KeyboardType.Email,
                    )

                    WickPasswordField(
                        value = uiState.password,
                        onValueChange = { onAction(RegisterAction.PasswordChanged(it)) },
                        placeholder = stringResource(R.string.register_password_placeholder),
                        isError = uiState.password.isNotEmpty() && !uiState.isPasswordStrongEnough,
                    )

                    if (uiState.password.isNotEmpty() && !uiState.isPasswordStrongEnough) {
                        Text(
                            text = stringResource(R.string.register_password_requirements),
                            style = WickTextStyles.BodySmall,
                            color = WickColors.TextSecondary,
                        )
                    }

                    WickPasswordField(
                        value = uiState.confirmPassword,
                        onValueChange = { onAction(RegisterAction.ConfirmPasswordChanged(it)) },
                        placeholder = stringResource(R.string.register_confirm_password_placeholder),
                        isError = !uiState.doPasswordsMatch,
                    )

                    if (!uiState.doPasswordsMatch) {
                        WickInlineWarning(text = stringResource(R.string.register_password_mismatch))
                    }

                    uiState.error?.let { error ->
                        WickInlineWarning(text = error.toDisplayMessage())
                    }

                    WickButton(
                        text = stringResource(R.string.register_submit_button),
                        onClick = { onAction(RegisterAction.Submit) },
                        enabled = uiState.canSubmit,
                        loading = uiState.isCheckingEmail,
                    )

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.register_login_prompt),
                            style = WickTextStyles.BodySmall,
                            color = WickColors.TextSecondary,
                        )
                        Text(
                            text = stringResource(R.string.register_login_link),
                            style = WickTextStyles.LinkBold,
                            color = WickColors.Primary,
                            modifier = Modifier.clickable(onClick = onNavigateToLogin),
                        )
                    }
            }
        }
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Conflict -> stringResource(R.string.register_error_conflict)
    UiError.RateLimited -> stringResource(R.string.register_error_rate_limited)
    UiError.NoConnection -> stringResource(R.string.register_error_no_connection)
    is UiError.Message -> text
    UiError.Unauthorized -> stringResource(R.string.register_error_unknown)
    UiError.Forbidden -> stringResource(R.string.register_error_unknown)
    UiError.Unknown, UiError.InvalidLink,
    UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens -> stringResource(R.string.register_error_unknown)
}
