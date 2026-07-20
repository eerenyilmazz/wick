package com.wick.feature.auth.legal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickCheckbox
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.LegalDocumentType
import com.wick.core.model.UiError
import com.wick.feature.auth.R

@Composable
fun LegalConsentRoute(
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    viewModel: LegalConsentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LegalConsentEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    LegalConsentScreen(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
private fun LegalConsentScreen(
    uiState: LegalConsentUiState,
    onAction: (LegalConsentAction) -> Unit,
    onBack: () -> Unit,
) {
    var selectedType by remember { mutableStateOf<LegalDocumentType?>(null) }
    var privacyPolicyRead by remember { mutableStateOf(false) }
    var termsOfServiceRead by remember { mutableStateOf(false) }
    val documentsRead = privacyPolicyRead && termsOfServiceRead

    BackHandler(enabled = selectedType != null) { selectedType = null }

    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar(onBackClick = if (selectedType != null) { { selectedType = null } } else onBack)

        if (uiState.isLoadingDocuments) {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WickColors.Primary,
                )
            }
            return@Column
        }

        if (selectedType != null) {
            val document = if (selectedType == LegalDocumentType.PRIVACY_POLICY) {
                uiState.privacyPolicy
            } else {
                uiState.termsOfService
            }
            val title = if (selectedType == LegalDocumentType.PRIVACY_POLICY) {
                stringResource(R.string.legal_privacy_policy_label)
            } else {
                stringResource(R.string.legal_terms_label)
            }

            document?.let {
                val scrollState = rememberScrollState()

                LaunchedEffect(selectedType, scrollState.maxValue, scrollState.value) {
                    if (scrollState.maxValue == 0 || scrollState.value >= scrollState.maxValue) {
                        if (selectedType == LegalDocumentType.PRIVACY_POLICY) {
                            privacyPolicyRead = true
                        } else {
                            termsOfServiceRead = true
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = title, style = WickTextStyles.HeadlineLarge, color = WickColors.TextPrimary)
                    Text(text = it.content, style = WickTextStyles.BodySmall, color = WickColors.TextSecondary)
                }
            }
            return@Column
        }

        Text(
            text = stringResource(R.string.legal_title),
            style = WickTextStyles.HeadlineLarge,
            color = WickColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Text(
            text = stringResource(R.string.legal_subtitle),
            style = WickTextStyles.Body,
            color = WickColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LegalDocumentRow(
                icon = Icons.AutoMirrored.Filled.Article,
                label = stringResource(R.string.legal_privacy_policy_label),
                read = privacyPolicyRead,
                onClick = { selectedType = LegalDocumentType.PRIVACY_POLICY },
            )
            LegalDocumentRow(
                icon = Icons.Filled.Gavel,
                label = stringResource(R.string.legal_terms_label),
                read = termsOfServiceRead,
                onClick = { selectedType = LegalDocumentType.TERMS_OF_SERVICE },
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.error?.let { error ->
                WickInlineWarning(text = error.toDisplayMessage())
            }

            if (!documentsRead) {
                Text(
                    text = stringResource(R.string.legal_read_required_hint),
                    style = WickTextStyles.BodySmall,
                    color = WickColors.TextMuted,
                )
            }

            WickCheckbox(
                checked = uiState.accepted,
                onCheckedChange = { onAction(LegalConsentAction.AcceptedChanged(it)) },
                label = stringResource(R.string.legal_checkbox_label),
                enabled = documentsRead,
            )

            WickButton(
                text = stringResource(R.string.legal_submit_button),
                onClick = { onAction(LegalConsentAction.Submit) },
                enabled = uiState.canSubmit && documentsRead,
                loading = uiState.isSubmitting,
            )
        }
    }
}

@Composable
private fun LegalDocumentRow(icon: ImageVector, label: String, read: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = WickColors.Primary, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = WickTextStyles.Body,
            color = WickColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (read) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.legal_document_read),
                tint = WickColors.Primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Conflict -> stringResource(R.string.legal_error_conflict)
    UiError.RateLimited -> stringResource(R.string.legal_error_rate_limited)
    UiError.NoConnection -> stringResource(R.string.legal_error_no_connection)
    is UiError.Message -> text
    UiError.Unauthorized -> stringResource(R.string.legal_error_unknown)
    UiError.Forbidden -> stringResource(R.string.legal_error_unknown)
    UiError.Unknown, UiError.InvalidLink,
    UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens -> stringResource(R.string.legal_error_unknown)
}
