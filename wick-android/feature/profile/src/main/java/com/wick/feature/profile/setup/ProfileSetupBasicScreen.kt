@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.wick.feature.profile.setup

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickSelectionSheet
import com.wick.core.designsystem.component.WickTiltedBadge
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.UiError
import com.wick.feature.profile.R
import java.io.File
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun ProfileSetupBasicRoute(
    onNavigateToBio: () -> Unit,
    viewModel: ProfileSetupBasicViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileSetupBasicEvent.NavigateToBio -> onNavigateToBio()
            }
        }
    }

    ProfileSetupBasicScreen(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
private fun ProfileSetupBasicScreen(
    uiState: ProfileSetupBasicUiState,
    onAction: (ProfileSetupBasicAction) -> Unit,
) {
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri: Uri? by rememberSaveable { mutableStateOf(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxOf(uiState.maxPhotos, 2)),
    ) { uris -> if (uris.isNotEmpty()) onAction(ProfileSetupBasicAction.PhotosPicked(uris)) }

    val cameraCapture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            onAction(ProfileSetupBasicAction.PhotoPicked(uri))
        }
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
        if (uiState.isLoadingConfig || uiState.isLoadingPhotos) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = WickColors.Primary,
            )
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            WickTopBar()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                WickTiltedBadge(
                    text = stringResource(R.string.profile_basic_welcome_badge),
                    modifier = Modifier.padding(top = 8.dp),
                )

                Text(
                    text = stringResource(R.string.profile_basic_title),
                    style = WickTextStyles.HeadlineLarge,
                    color = WickColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.profile_basic_subtitle),
                    style = WickTextStyles.Body,
                    color = WickColors.TextSecondary,
                )

                PhotoGridSection(
                    photos = uiState.photos,
                    canAddMorePhotos = uiState.canAddMorePhotos,
                    isUploadingPhoto = uiState.isUploadingPhoto,
                    onAddPhoto = { showPhotoSourceDialog = true },
                    onRemovePhoto = { onAction(ProfileSetupBasicAction.PhotoRemoved(it)) },
                    onMovePhoto = { from, to -> onAction(ProfileSetupBasicAction.PhotoMoved(from, to)) },
                )

                BasicInfoFormSection(uiState = uiState, onAction = onAction)

                uiState.error?.let { error ->
                    WickInlineWarning(text = error.toDisplayMessage())
                }
            }

            WickButton(
                text = stringResource(R.string.profile_basic_submit_button),
                onClick = { onAction(ProfileSetupBasicAction.Submit) },
                enabled = uiState.canSubmit,
                loading = uiState.isSubmitting,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }

        if (uiState.showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { onAction(ProfileSetupBasicAction.DatePickerVisibilityChanged(false)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                                onAction(ProfileSetupBasicAction.DateOfBirthChanged(date))
                            }
                        },
                    ) {
                        Text(stringResource(R.string.profile_basic_date_picker_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(ProfileSetupBasicAction.DatePickerVisibilityChanged(false)) }) {
                        Text(stringResource(R.string.profile_basic_date_picker_cancel))
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showPhotoSourceDialog) {
            PhotoSourceDialog(
                onDismiss = { showPhotoSourceDialog = false },
                onGallerySelected = {
                    showPhotoSourceDialog = false
                    galleryPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onCameraSelected = {
                    showPhotoSourceDialog = false
                    cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)
                },
            )
        }

        if (uiState.showProvincePicker) {
            WickSelectionSheet(
                title = stringResource(R.string.profile_basic_province_label),
                options = uiState.provinces,
                searchPlaceholder = stringResource(R.string.profile_basic_location_search_placeholder),
                emptyMessage = stringResource(R.string.profile_basic_location_empty),
                onOptionSelected = { onAction(ProfileSetupBasicAction.ProvinceSelected(it)) },
                onDismiss = { onAction(ProfileSetupBasicAction.ProvincePickerVisibilityChanged(false)) },
                selectedValue = uiState.province,
            )
        }

        if (uiState.showDistrictPicker) {
            WickSelectionSheet(
                title = stringResource(R.string.profile_basic_district_label),
                options = uiState.districts,
                searchPlaceholder = stringResource(R.string.profile_basic_location_search_placeholder),
                emptyMessage = stringResource(R.string.profile_basic_location_empty),
                onOptionSelected = { onAction(ProfileSetupBasicAction.DistrictSelected(it)) },
                onDismiss = { onAction(ProfileSetupBasicAction.DistrictPickerVisibilityChanged(false)) },
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
    UiError.Unauthorized -> stringResource(R.string.profile_basic_error_unauthorized)
    UiError.NoConnection -> stringResource(R.string.profile_basic_error_no_connection)
    UiError.Conflict -> stringResource(R.string.profile_bio_error_max_photos)
    is UiError.Message -> text
    UiError.RateLimited, UiError.Unknown, UiError.Forbidden,
    UiError.InvalidLink, UiError.ProductNotFound, UiError.SessionNotFound, UiError.InsufficientTokens ->
        stringResource(R.string.profile_basic_error_unknown)
}
