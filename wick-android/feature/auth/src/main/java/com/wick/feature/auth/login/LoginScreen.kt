package com.wick.feature.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickCheckbox
import com.wick.core.designsystem.component.WickEnterTransition
import com.wick.core.designsystem.component.WickInlineSuccess
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickLogoMark
import com.wick.core.designsystem.component.WickPasswordField
import com.wick.core.designsystem.component.WickTextField
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.UiError
import com.wick.feature.auth.R

@Composable
fun LoginRoute(
    onNavigateToDiscover: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    registeredSuccessfully: Boolean = false,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToDiscover -> onNavigateToDiscover()
                LoginEvent.NavigateToProfileSetup -> onNavigateToProfileSetup()
            }
        }
    }

    LoginScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        registeredSuccessfully = registeredSuccessfully,
    )
}

@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    onAction: (LoginAction) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    registeredSuccessfully: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        WickTopBar()

        WickEnterTransition {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                WickLogoMark()

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.login_title),
                    style = WickTextStyles.HeadlineLarge,
                    color = WickColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = WickTextStyles.Body,
                    color = WickColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(40.dp))

                WickTextField(
                    value = uiState.email,
                    onValueChange = { onAction(LoginAction.EmailChanged(it)) },
                    placeholder = stringResource(R.string.login_email_placeholder),
                    keyboardType = KeyboardType.Email,
                )

                Spacer(modifier = Modifier.height(12.dp))

                WickPasswordField(
                    value = uiState.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    placeholder = stringResource(R.string.login_password_placeholder),
                    isError = uiState.error != null,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WickCheckbox(
                        checked = uiState.keepSignedIn,
                        onCheckedChange = { onAction(LoginAction.KeepSignedInChanged(it)) },
                        label = stringResource(R.string.login_keep_signed_in),
                    )
                    Text(
                        text = stringResource(R.string.login_forgot_password),
                        style = WickTextStyles.Label,
                        color = WickColors.Primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.clickable(onClick = onNavigateToForgotPassword),
                    )
                }

                if (registeredSuccessfully) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WickInlineSuccess(text = stringResource(R.string.login_register_success))
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    WickInlineWarning(text = error.toDisplayMessage())
                }

                Spacer(modifier = Modifier.height(32.dp))

                WickButton(
                    text = stringResource(R.string.login_submit_button),
                    onClick = { onAction(LoginAction.Submit) },
                    enabled = uiState.canSubmit,
                    loading = uiState.isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.login_register_prompt),
                        style = WickTextStyles.BodySmall,
                        color = WickColors.TextSecondary,
                    )
                    Text(
                        text = stringResource(R.string.login_register_link),
                        style = WickTextStyles.LinkBold,
                        color = WickColors.Primary,
                        modifier = Modifier.clickable(onClick = onNavigateToRegister),
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.login_error_unauthorized)
    UiError.RateLimited -> stringResource(R.string.login_error_rate_limited)
    UiError.NoConnection -> stringResource(R.string.login_error_no_connection)
    is UiError.Message -> text
    UiError.Conflict -> stringResource(R.string.login_error_unknown)
    UiError.Forbidden -> stringResource(R.string.login_error_unknown)
    UiError.Unknown, UiError.InvalidLink,
    UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens -> stringResource(R.string.login_error_unknown)
}
