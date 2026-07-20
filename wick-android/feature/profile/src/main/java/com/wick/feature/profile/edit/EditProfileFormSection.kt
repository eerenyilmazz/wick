package com.wick.feature.profile.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickLocationRow
import com.wick.core.designsystem.component.WickSegment
import com.wick.core.designsystem.component.WickSegmentedControl
import com.wick.core.designsystem.component.WickTextField
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.Gender
import com.wick.feature.profile.R
import com.wick.feature.profile.setup.BIO_MAX_LENGTH
import com.wick.feature.profile.setup.InterestsSection

@Composable
fun EditProfileFormSection(
    uiState: EditProfileUiState,
    onAction: (EditProfileAction) -> Unit,
) {
    Text(
        text = stringResource(R.string.edit_profile_name_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    WickTextField(
        value = uiState.displayName,
        onValueChange = { onAction(EditProfileAction.DisplayNameChanged(it)) },
        placeholder = stringResource(R.string.edit_profile_name_placeholder),
    )

    Text(
        text = stringResource(R.string.edit_profile_bio_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    Box {
        WickTextField(
            value = uiState.bio,
            onValueChange = { onAction(EditProfileAction.BioChanged(it)) },
            placeholder = stringResource(R.string.edit_profile_bio_placeholder),
            singleLine = false,
            modifier = Modifier.height(140.dp),
        )
        Text(
            text = stringResource(R.string.edit_profile_bio_character_count, uiState.bio.length, BIO_MAX_LENGTH),
            style = WickTextStyles.BodySmall,
            color = WickColors.TextMuted,
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
        )
    }

    Text(
        text = stringResource(R.string.edit_profile_gender_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    WickSegmentedControl(
        segments = listOf(
            WickSegment(Gender.FEMALE, stringResource(R.string.edit_profile_gender_female)),
            WickSegment(Gender.MALE, stringResource(R.string.edit_profile_gender_male)),
            WickSegment(Gender.OTHER, stringResource(R.string.edit_profile_gender_other)),
        ),
        selected = uiState.gender,
        onSelected = { onAction(EditProfileAction.GenderChanged(it)) },
    )

    Text(
        text = stringResource(R.string.edit_profile_location_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    WickLocationRow(
        placeholder = stringResource(R.string.edit_profile_province_placeholder),
        value = uiState.province,
        onClick = { onAction(EditProfileAction.ProvincePickerVisibilityChanged(true)) },
    )

    WickLocationRow(
        placeholder = stringResource(R.string.edit_profile_district_placeholder),
        value = uiState.district,
        enabled = uiState.province != null,
        onClick = { onAction(EditProfileAction.DistrictPickerVisibilityChanged(true)) },
    )

    Text(
        text = stringResource(R.string.edit_profile_interests_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    InterestsSection(
        interests = uiState.interests,
        interestInput = uiState.interestInput,
        canAddMoreInterests = uiState.canAddMoreInterests,
        onInputChanged = { onAction(EditProfileAction.InterestInputChanged(it)) },
        onInterestAdded = { onAction(EditProfileAction.InterestAdded(it)) },
        onInterestRemoved = { onAction(EditProfileAction.InterestRemoved(it)) },
    )
}
