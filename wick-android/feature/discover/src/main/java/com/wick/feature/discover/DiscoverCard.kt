package com.wick.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.component.WickVerifiedBadge
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.User
import timber.log.Timber
import java.time.LocalDate
import java.time.Period

@Composable
fun DiscoverCard(user: User, modifier: Modifier = Modifier) {
    val age = user.dateOfBirth?.let { rawDate ->
        runCatching { Period.between(LocalDate.parse(rawDate), LocalDate.now()).years }
            .getOrNull()
    }
    val location = listOfNotNull(user.province, user.district).joinToString(", ")
    val distanceText = user.distanceKm?.let { km ->
        if (km < 1) {
            stringResource(R.string.discover_distance_nearby)
        } else {
            stringResource(R.string.discover_distance_km, km.toInt())
        }
    }
    val locationLine = listOfNotNull(location.takeIf { it.isNotEmpty() }, distanceText).joinToString(" · ")

    Box(
        modifier = modifier
            .clip(WickShapes.Card)
            .background(WickColors.Surface),
    ) {
        val photoUrl = user.photos.firstOrNull()?.url
        WickAsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            onError = { throwable -> Timber.e(throwable, "Discover photo load failed: %s", photoUrl) },
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(WickColors.SurfaceElevated.copy(alpha = 0.78f))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (age != null) {
                        stringResource(R.string.discover_age_location, user.displayName, age)
                    } else {
                        user.displayName
                    },
                    style = WickTextStyles.HeadlineMedium,
                    color = WickColors.TextPrimary,
                )
                if (user.verificationStatus == "VERIFIED") {
                    WickVerifiedBadge(contentDescription = stringResource(R.string.profile_detail_verified))
                }
            }

            if (locationLine.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = WickColors.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = locationLine,
                        style = WickTextStyles.BodySmall,
                        color = WickColors.TextSecondary,
                    )
                }
            }
        }

        if (user.isNew) {
            Text(
                text = stringResource(R.string.discover_new_badge),
                style = WickTextStyles.Label,
                color = WickColors.OnPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(WickShapes.Chip)
                    .background(WickColors.Primary)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
