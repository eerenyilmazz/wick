@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.wick.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.component.WickInlineWarning
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.Gender

private const val RADIUS_STEP_KM = 5
private const val RADIUS_MIN_KM = 5
private const val RADIUS_MAX_KM = 300
private const val AGE_MIN = 18
private const val AGE_MAX = 100
private const val AGE_MIN_GAP = 1

@Composable
fun DiscoverFilterSheet(uiState: DiscoverUiState, onAction: (DiscoverAction) -> Unit) {
    val requestLocation = rememberLocationRequester(
        onLocationReceived = { lat, lng -> onAction(DiscoverAction.LocationReceived(lat, lng)) },
        onDenied = { onAction(DiscoverAction.LocationDenied) },
    )

    Dialog(
        onDismissRequest = { onAction(DiscoverAction.DismissFilters) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WickColors.Background.copy(alpha = 0.7f))
                .clickable(onClick = { onAction(DiscoverAction.DismissFilters) }),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(WickShapes.Sheet)
                    .background(WickColors.Background)
                    .clickable(enabled = false, onClick = {}),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onAction(DiscoverAction.DismissFilters) }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = WickColors.TextPrimary)
                    }
                    Text(
                        text = stringResource(R.string.discover_filter_title_short),
                        style = WickTextStyles.HeadlineMedium,
                        color = WickColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(R.string.discover_filter_reset),
                        style = WickTextStyles.Label,
                        color = WickColors.Primary,
                        modifier = Modifier
                            .clip(WickShapes.Chip)
                            .clickable(onClick = { onAction(DiscoverAction.ResetFilters) })
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                }

                if (uiState.isFilterLoading) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WickColors.Primary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FilterCard { AgeRangeSection(uiState = uiState, onAction = onAction) }
                        FilterCard {
                            DistanceSection(uiState = uiState, onAction = onAction, onRequestLocation = requestLocation)
                        }
                        FilterCard { GenderSection(uiState = uiState, onAction = onAction) }
                        SmartMatchCard()
                    }

                    WickButton(
                        text = stringResource(R.string.discover_filter_apply),
                        onClick = { onAction(DiscoverAction.ApplyFilters) },
                        enabled = !uiState.isFilterSaving && uiState.filterMinAge <= uiState.filterMaxAge,
                        loading = uiState.isFilterSaving,
                        variant = WickButtonVariant.Primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                            .padding(bottom = 40.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Surface)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = { content() },
    )
}

@Composable
private fun SectionHeader(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = WickTextStyles.HeadlineMedium, color = WickColors.TextPrimary)
        Text(text = value, style = WickTextStyles.HeadlineMedium, color = WickColors.Primary)
    }
}

@Composable
private fun wickSliderColors() = SliderDefaults.colors(
    thumbColor = WickColors.Primary,
    activeTrackColor = WickColors.Primary,
    inactiveTrackColor = WickColors.Border,
)

@Composable
private fun AgeRangeSection(uiState: DiscoverUiState, onAction: (DiscoverAction) -> Unit) {
    SectionHeader(
        label = stringResource(R.string.discover_filter_age_label),
        value = stringResource(R.string.discover_filter_age_value, uiState.filterMinAge, uiState.filterMaxAge),
    )

    RangeSlider(
        value = uiState.filterMinAge.toFloat()..uiState.filterMaxAge.toFloat(),
        onValueChange = { range ->
            val newMin = range.start.toInt()
            val newMax = range.endInclusive.toInt()
            if (newMax - newMin >= AGE_MIN_GAP) {
                onAction(DiscoverAction.FilterMinAgeChanged(newMin))
                onAction(DiscoverAction.FilterMaxAgeChanged(newMax))
            }
        },
        valueRange = AGE_MIN.toFloat()..AGE_MAX.toFloat(),
        colors = wickSliderColors(),
    )
}

@Composable
private fun DistanceSection(
    uiState: DiscoverUiState,
    onAction: (DiscoverAction) -> Unit,
    onRequestLocation: () -> Unit,
) {
    if (!uiState.hasSharedLocation) {
        Text(text = stringResource(R.string.discover_filter_distance_label), style = WickTextStyles.Label, color = WickColors.TextSecondary)
        WickButton(
            text = stringResource(R.string.discover_filter_share_location),
            onClick = onRequestLocation,
            enabled = !uiState.isRequestingLocation,
            loading = uiState.isRequestingLocation,
            variant = WickButtonVariant.Secondary,
        )
        if (uiState.locationDenied) {
            WickInlineWarning(text = stringResource(R.string.discover_filter_location_denied))
        }
    } else {
        val radiusKm = uiState.filterRadiusKm ?: RADIUS_MAX_KM

        SectionHeader(
            label = stringResource(R.string.discover_filter_distance_label),
            value = stringResource(R.string.discover_filter_distance_value, radiusKm),
        )

        Slider(
            value = radiusKm.toFloat(),
            onValueChange = { value ->
                val rounded = (Math.round(value / RADIUS_STEP_KM) * RADIUS_STEP_KM).coerceIn(RADIUS_MIN_KM, RADIUS_MAX_KM)
                onAction(DiscoverAction.FilterRadiusChanged(rounded))
            },
            valueRange = RADIUS_MIN_KM.toFloat()..RADIUS_MAX_KM.toFloat(),
            colors = wickSliderColors(),
        )
    }
}

@Composable
private fun GenderSection(uiState: DiscoverUiState, onAction: (DiscoverAction) -> Unit) {
    Text(
        text = stringResource(R.string.discover_filter_gender_label),
        style = WickTextStyles.HeadlineMedium,
        color = WickColors.TextPrimary,
    )

    data class GenderOption(val label: String, val genders: Set<Gender>)
    val options = listOf(
        GenderOption(stringResource(R.string.discover_filter_gender_female), setOf(Gender.FEMALE)),
        GenderOption(stringResource(R.string.discover_filter_gender_male), setOf(Gender.MALE)),
        GenderOption(stringResource(R.string.discover_filter_gender_everyone), emptySet()),
    )
    val currentGenders = uiState.filterGenders

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .background(WickColors.Background),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        options.forEach { option ->
            val selected = currentGenders == option.genders
            Text(
                text = option.label,
                style = WickTextStyles.Label,
                color = if (selected) WickColors.OnPrimary else WickColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(WickShapes.Chip)
                    .background(if (selected) WickColors.Primary else WickColors.Background)
                    .clickable { onAction(DiscoverAction.FilterGenderSet(option.genders)) }
                    .padding(vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun SmartMatchCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WickShapes.Card)
            .border(1.dp, WickColors.Border, WickShapes.Card)
            .background(WickColors.Surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(WickShapes.Chip)
                .background(WickColors.Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = WickColors.Primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = stringResource(R.string.discover_filter_smart_match_title),
                style = WickTextStyles.Label,
                color = WickColors.TextPrimary,
            )
            Text(
                text = stringResource(R.string.discover_filter_smart_match_body),
                style = WickTextStyles.BodySmall,
                color = WickColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun Gender.toDisplayLabel(): String = when (this) {
    Gender.MALE -> stringResource(R.string.discover_filter_gender_male)
    Gender.FEMALE -> stringResource(R.string.discover_filter_gender_female)
    Gender.OTHER -> stringResource(R.string.discover_filter_gender_other)
}
