@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.wick.feature.profile.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickChip
import com.wick.core.designsystem.component.WickTextField
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.profile.R

@Composable
fun InterestsSection(
    interests: List<String>,
    interestInput: String,
    canAddMoreInterests: Boolean,
    onInputChanged: (String) -> Unit,
    onInterestAdded: (String) -> Unit,
    onInterestRemoved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (interests.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                interests.forEach { interest ->
                    RemovableInterestChip(
                        text = interest,
                        onRemove = { onInterestRemoved(interest) },
                    )
                }
            }
        }

        if (canAddMoreInterests) {
            WickTextField(
                value = interestInput,
                onValueChange = onInputChanged,
                placeholder = stringResource(R.string.profile_basic_interest_placeholder),
            )

            val trimmedInput = interestInput.trim()
            val showCustomSuggestion = trimmedInput.isNotEmpty() &&
                interests.none { it.equals(trimmedInput, ignoreCase = true) }

            if (showCustomSuggestion) {
                WickChip(
                    text = stringResource(R.string.profile_basic_interest_add, trimmedInput),
                    selected = false,
                    onClick = { onInterestAdded(trimmedInput) },
                )
            }
        }
    }
}

@Composable
private fun RemovableInterestChip(text: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(WickShapes.Chip)
            .background(WickColors.Primary)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = text, style = WickTextStyles.Label, color = WickColors.OnPrimary)
        Box(
            modifier = Modifier.size(32.dp).clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.profile_basic_interest_remove),
                tint = WickColors.OnPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
