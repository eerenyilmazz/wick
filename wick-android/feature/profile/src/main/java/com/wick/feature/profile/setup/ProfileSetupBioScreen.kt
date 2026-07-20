package com.wick.feature.profile.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickLogoMark
import com.wick.core.designsystem.component.WickTextField
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.UiError
import com.wick.feature.profile.R

@Composable
fun ProfileSetupBioRoute(
    onNavigateToDiscover: () -> Unit,
    viewModel: ProfileSetupBioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileSetupBioEvent.NavigateToDiscover -> onNavigateToDiscover()
            }
        }
    }

    ProfileSetupBioScreen(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
private fun ProfileSetupBioScreen(
    uiState: ProfileSetupBioUiState,
    onAction: (ProfileSetupBioAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WickLogoMark(modifier = Modifier.height(56.dp))

            Text(
                text = stringResource(R.string.profile_bio_title),
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
            )
            Text(
                text = stringResource(R.string.profile_bio_subtitle),
                style = WickTextStyles.Body,
                color = WickColors.TextSecondary,
            )

            Box {
                WickTextField(
                    value = uiState.bio,
                    onValueChange = { onAction(ProfileSetupBioAction.BioChanged(it)) },
                    placeholder = stringResource(R.string.profile_bio_placeholder),
                    singleLine = false,
                    modifier = Modifier.height(180.dp),
                )
                Text(
                    text = stringResource(R.string.profile_bio_character_count, uiState.bio.length, BIO_MAX_LENGTH),
                    style = WickTextStyles.BodySmall,
                    color = WickColors.TextMuted,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                )
            }

            Text(
                text = stringResource(R.string.profile_bio_min_length_hint, BIO_MIN_LENGTH),
                style = WickTextStyles.BodySmall,
                color = WickColors.TextMuted,
            )

            uiState.error?.let { error ->
                WickInlineWarning(text = error.toDisplayMessage())
            }
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WickButton(
                text = stringResource(R.string.profile_bio_submit_button),
                onClick = { onAction(ProfileSetupBioAction.Submit) },
                enabled = uiState.canSubmit,
                loading = uiState.isSubmitting,
            )
        }
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.profile_bio_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.profile_bio_error_no_connection)
    UiError.Conflict -> stringResource(R.string.profile_bio_error_max_photos)
    is UiError.Message -> text
    UiError.RateLimited, UiError.Unknown, UiError.Forbidden,
    UiError.InvalidLink, UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens ->
        stringResource(R.string.profile_bio_error_unknown)
}
