package com.wick.feature.profile.legal

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
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.LegalDocumentType
import com.wick.feature.profile.R

@Composable
fun LegalDocumentsRoute(
    onBack: () -> Unit,
    viewModel: LegalDocumentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LegalDocumentsScreen(uiState = uiState, onBack = onBack)
}

@Composable
private fun LegalDocumentsScreen(uiState: LegalDocumentsUiState, onBack: () -> Unit) {
    var selectedType by remember { mutableStateOf<LegalDocumentType?>(null) }

    BackHandler(enabled = selectedType != null) { selectedType = null }

    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar(onBackClick = { if (selectedType != null) selectedType = null else onBack() })

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = WickColors.Primary,
                )

                uiState.error != null -> WickInlineWarning(
                    text = stringResource(R.string.legal_documents_error_unknown),
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 20.dp),
                )

                selectedType != null -> {
                    val document = if (selectedType == LegalDocumentType.PRIVACY_POLICY) {
                        uiState.privacyPolicy
                    } else {
                        uiState.termsOfService
                    }
                    val title = if (selectedType == LegalDocumentType.PRIVACY_POLICY) {
                        stringResource(R.string.legal_documents_privacy_policy_label)
                    } else {
                        stringResource(R.string.legal_documents_terms_label)
                    }

                    document?.let {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(text = title, style = WickTextStyles.HeadlineLarge, color = WickColors.TextPrimary)
                            Text(text = it.content, style = WickTextStyles.BodySmall, color = WickColors.TextSecondary)
                        }
                    }
                }

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.legal_documents_title),
                        style = WickTextStyles.HeadlineLarge,
                        color = WickColors.TextPrimary,
                    )
                    LegalDocumentRow(
                        icon = Icons.AutoMirrored.Filled.Article,
                        label = stringResource(R.string.legal_documents_privacy_policy_label),
                        onClick = { selectedType = LegalDocumentType.PRIVACY_POLICY },
                    )
                    LegalDocumentRow(
                        icon = Icons.Filled.Gavel,
                        label = stringResource(R.string.legal_documents_terms_label),
                        onClick = { selectedType = LegalDocumentType.TERMS_OF_SERVICE },
                    )
                }
            }
        }
    }
}

@Composable
private fun LegalDocumentRow(icon: ImageVector, label: String, onClick: () -> Unit) {
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
    }
}
