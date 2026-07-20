package com.wick.feature.profile.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickPasswordField
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.profile.R

@Composable
fun AccountSecurityRoute(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AccountSecurityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AccountSecurityEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    AccountSecurityScreen(
        state = state,
        onPasswordChanged = viewModel::onPasswordChanged,
        onDeleteAccount = viewModel::deleteAccount,
        onBack = onBack,
    )
}

@Composable
private fun AccountSecurityScreen(
    state: AccountSecurityUiState,
    onPasswordChanged: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WickColors.Background),
    ) {
        WickTopBar(
            title = stringResource(R.string.account_security_title),
            onBackClick = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.size(40.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(WickShapes.Card)
                    .background(WickColors.Urgency.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = WickColors.Urgency,
                    modifier = Modifier.size(44.dp),
                )
            }

            Spacer(modifier = Modifier.size(28.dp))

            Text(
                text = stringResource(R.string.account_security_heading),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = stringResource(R.string.account_security_body),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.size(32.dp))

            WickPasswordField(
                value = state.password,
                onValueChange = onPasswordChanged,
                placeholder = stringResource(R.string.account_security_password_placeholder),
                isError = state.hasError,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.hasError) {
                Spacer(modifier = Modifier.size(8.dp))
                WickInlineWarning(text = stringResource(R.string.account_security_password_error))
            }

            Spacer(modifier = Modifier.size(20.dp))

            WickButton(
                text = stringResource(R.string.account_security_cta),
                onClick = onDeleteAccount,
                variant = WickButtonVariant.Danger,
                enabled = state.password.isNotBlank() && !state.isDeleting,
                loading = state.isDeleting,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.size(16.dp))

            WickButton(
                text = stringResource(R.string.account_security_cancel),
                onClick = onBack,
                variant = WickButtonVariant.Secondary,
                enabled = !state.isDeleting,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}
