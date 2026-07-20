package com.wick.feature.profile.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.component.WickLocationRow
import com.wick.core.designsystem.component.WickSegment
import com.wick.core.designsystem.component.WickSegmentedControl
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.Gender
import com.wick.feature.profile.R
import java.time.format.DateTimeFormatter

@Composable
fun BasicInfoFormSection(
    uiState: ProfileSetupBasicUiState,
    onAction: (ProfileSetupBasicAction) -> Unit,
) {
    Text(
        text = stringResource(R.string.profile_basic_date_of_birth_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.IconBox)
            .background(WickColors.Surface)
            .clickable { onAction(ProfileSetupBasicAction.DatePickerVisibilityChanged(true)) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = uiState.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd / MM / yyyy"))
                ?: stringResource(R.string.profile_basic_date_of_birth_placeholder),
            style = WickTextStyles.Body,
            color = if (uiState.dateOfBirth != null) WickColors.TextPrimary else WickColors.TextMuted,
        )
        Icon(
            imageVector = Icons.Filled.CalendarToday,
            contentDescription = null,
            tint = WickColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }

    if (uiState.dateOfBirth != null && !uiState.isAgeValid) {
        WickInlineWarning(
            text = stringResource(R.string.profile_basic_age_invalid, uiState.minimumAge, uiState.maximumAge),
        )
    }

    Text(
        text = stringResource(R.string.profile_basic_gender_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    WickSegmentedControl(
        segments = listOf(
            WickSegment(Gender.FEMALE, stringResource(R.string.profile_basic_gender_female)),
            WickSegment(Gender.MALE, stringResource(R.string.profile_basic_gender_male)),
            WickSegment(Gender.OTHER, stringResource(R.string.profile_basic_gender_other)),
        ),
        selected = uiState.gender,
        onSelected = { onAction(ProfileSetupBasicAction.GenderChanged(it)) },
    )

    Text(
        text = stringResource(R.string.profile_basic_location_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )

    WickLocationRow(
        placeholder = stringResource(R.string.profile_basic_province_placeholder),
        value = uiState.province,
        onClick = { onAction(ProfileSetupBasicAction.ProvincePickerVisibilityChanged(true)) },
    )

    WickLocationRow(
        placeholder = stringResource(R.string.profile_basic_district_placeholder),
        value = uiState.district,
        enabled = uiState.province != null,
        onClick = { onAction(ProfileSetupBasicAction.DistrictPickerVisibilityChanged(true)) },
    )

    Text(
        text = stringResource(R.string.profile_basic_interests_label),
        style = WickTextStyles.Label,
        color = WickColors.TextSecondary,
    )
    Text(
        text = stringResource(R.string.profile_basic_interests_hint),
        style = WickTextStyles.BodySmall,
        color = WickColors.TextMuted,
    )

    InterestsSection(
        interests = uiState.interests,
        interestInput = uiState.interestInput,
        canAddMoreInterests = uiState.canAddMoreInterests,
        onInputChanged = { onAction(ProfileSetupBasicAction.InterestInputChanged(it)) },
        onInterestAdded = { onAction(ProfileSetupBasicAction.InterestAdded(it)) },
        onInterestRemoved = { onAction(ProfileSetupBasicAction.InterestRemoved(it)) },
    )
}
