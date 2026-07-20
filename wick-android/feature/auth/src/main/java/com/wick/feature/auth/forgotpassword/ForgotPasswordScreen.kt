package com.wick.feature.auth.forgotpassword

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickEnterTransition
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.model.UiError
import com.wick.core.designsystem.component.WickLogoMark
import com.wick.core.designsystem.component.WickTextField
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.auth.R

@Composable
fun ForgotPasswordRoute(
    onBack: () -> Unit,
    onNavigateToEmailSent: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ForgotPasswordEvent.NavigateToEmailSent -> onNavigateToEmailSent(event.email)
            }
        }
    }

    ForgotPasswordScreen(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
private fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onAction: (ForgotPasswordAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        WickTopBar(onBackClick = onBack)

        WickEnterTransition {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                    WickLogoMark()

                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        style = WickTextStyles.HeadlineLarge,
                        color = WickColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(R.string.forgot_password_subtitle),
                        style = WickTextStyles.Body,
                        color = WickColors.TextSecondary,
                    )

                    WickTextField(
                        value = uiState.email,
                        onValueChange = { onAction(ForgotPasswordAction.EmailChanged(it)) },
                        placeholder = stringResource(R.string.forgot_password_email_placeholder),
                        keyboardType = KeyboardType.Email,
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = WickColors.TextMuted)
                        },
                    )

                    uiState.error?.let { WickInlineWarning(text = it.toDisplayMessage()) }

                    WickButton(
                        text = stringResource(R.string.forgot_password_submit_button),
                        onClick = { onAction(ForgotPasswordAction.Submit) },
                        enabled = uiState.canSubmit,
                        loading = uiState.isLoading,
                        modifier = if (uiState.canSubmit) {
                            Modifier.shadow(
                                elevation = 12.dp,
                                shape = WickShapes.Button,
                                ambientColor = WickColors.Primary,
                                spotColor = WickColors.Primary,
                            )
                        } else {
                            Modifier
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_password_support_prompt),
                            style = WickTextStyles.BodySmall,
                            color = WickColors.TextSecondary,
                        )
                        Text(
                            text = " " + stringResource(R.string.forgot_password_support_link),
                            style = WickTextStyles.LinkBold,
                            color = WickColors.Primary,
                            modifier = Modifier.clickable { },
                        )
                    }
            }
        }
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.NoConnection -> stringResource(R.string.forgot_password_error_no_connection)
    else -> stringResource(R.string.forgot_password_error_unknown)
}
