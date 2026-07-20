@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.wick.feature.profile.edit

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickSelectionSheet
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.UiError
import com.wick.feature.profile.R
import com.wick.feature.profile.setup.PhotoGridSection
import com.wick.feature.profile.setup.PhotoSourceDialog
import java.io.File

@Composable
fun EditProfileRoute(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.NavigateBack -> onBack()
            }
        }
    }

    EditProfileScreen(uiState = uiState, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
private fun EditProfileScreen(
    uiState: EditProfileUiState,
    onAction: (EditProfileAction) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri: Uri? by rememberSaveable { mutableStateOf(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxOf(uiState.maxPhotos, 2)),
    ) { uris -> if (uris.isNotEmpty()) onAction(EditProfileAction.PhotosPicked(uris)) }

    val cameraCapture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) onAction(EditProfileAction.PhotoPicked(uri))
        pendingCameraUri = null
    }

    val cameraPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val uri = createCameraOutputUri(context)
            pendingCameraUri = uri
            cameraCapture.launch(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = WickColors.Primary)
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            WickTopBar(
                onBackClick = onBack,
                trailingContent = {
                    Text(
                        text = stringResource(R.string.edit_profile_save),
                        style = WickTextStyles.Label,
                        color = if (uiState.canSubmit) WickColors.OnPrimary else WickColors.TextMuted,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(WickShapes.Chip)
                            .background(if (uiState.canSubmit) WickColors.Primary else WickColors.Surface)
                            .clickable(enabled = uiState.canSubmit) { onAction(EditProfileAction.Submit) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.edit_profile_title),
                    style = WickTextStyles.HeadlineLarge,
                    color = WickColors.TextPrimary,
                )

                Text(
                    text = stringResource(R.string.edit_profile_photos_label),
                    style = WickTextStyles.Label,
                    color = WickColors.TextSecondary,
                )

                PhotoGridSection(
                    photos = uiState.visiblePhotos,
                    canAddMorePhotos = uiState.canAddMorePhotos,
                    isUploadingPhoto = uiState.isUploadingPhoto,
                    onAddPhoto = { showPhotoSourceDialog = true },
                    onRemovePhoto = { onAction(EditProfileAction.PhotoRemoved(it)) },
                    onSetAsCover = { onAction(EditProfileAction.PhotoSetAsCover(it)) },
                    onMovePhoto = { from, to -> onAction(EditProfileAction.PhotoMoved(from, to)) },
                )

                EditProfileFormSection(uiState = uiState, onAction = onAction)

                uiState.error?.let { error ->
                    WickInlineWarning(text = error.toDisplayMessage())
                }
            }
        }

        if (showPhotoSourceDialog) {
            PhotoSourceDialog(
                onDismiss = { showPhotoSourceDialog = false },
                onGallerySelected = {
                    showPhotoSourceDialog = false
                    galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onCameraSelected = {
                    showPhotoSourceDialog = false
                    cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)
                },
            )
        }

        if (uiState.showProvincePicker) {
            WickSelectionSheet(
                title = stringResource(R.string.edit_profile_province_placeholder),
                options = uiState.provinces,
                searchPlaceholder = stringResource(R.string.profile_basic_location_search_placeholder),
                emptyMessage = stringResource(R.string.profile_basic_location_empty),
                onOptionSelected = { onAction(EditProfileAction.ProvinceSelected(it)) },
                onDismiss = { onAction(EditProfileAction.ProvincePickerVisibilityChanged(false)) },
                selectedValue = uiState.province,
            )
        }

        if (uiState.showDistrictPicker) {
            WickSelectionSheet(
                title = stringResource(R.string.edit_profile_district_placeholder),
                options = uiState.districts,
                searchPlaceholder = stringResource(R.string.profile_basic_location_search_placeholder),
                emptyMessage = stringResource(R.string.profile_basic_location_empty),
                onOptionSelected = { onAction(EditProfileAction.DistrictSelected(it)) },
                onDismiss = { onAction(EditProfileAction.DistrictPickerVisibilityChanged(false)) },
                selectedValue = uiState.district,
            )
        }
    }
}

private fun createCameraOutputUri(context: Context): Uri {
    val directory = File(context.cacheDir, "captured_photos").apply { mkdirs() }
    val file = File.createTempFile("capture_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun UiError.toDisplayMessage(): String = when (this) {
    UiError.Unauthorized -> stringResource(R.string.edit_profile_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.edit_profile_error_no_connection)
    UiError.Conflict -> stringResource(R.string.profile_bio_error_max_photos)
    is UiError.Message -> text
    UiError.RateLimited, UiError.Unknown, UiError.Forbidden,
    UiError.InvalidLink, UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens ->
        stringResource(R.string.edit_profile_error_unknown)
}
