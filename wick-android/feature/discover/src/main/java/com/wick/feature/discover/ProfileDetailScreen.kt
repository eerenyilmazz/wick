package com.wick.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickPhotoGallery
import com.wick.core.designsystem.component.WickSectionLabel
import com.wick.core.designsystem.component.WickVerifiedBadge
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.ReportReason
import com.wick.core.model.User
import java.time.LocalDate
import java.time.Period

@Composable
fun ProfileDetailScreen(
    user: User,
    onBack: () -> Unit,
    onPass: () -> Unit = {},
    onLike: () -> Unit = {},
    onSuperLike: () -> Unit = {},
    onBlock: () -> Unit = {},
    onReport: (ReportReason) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val age = user.dateOfBirth?.let { rawDate ->
        runCatching { Period.between(LocalDate.parse(rawDate), LocalDate.now()).years }.getOrNull()
    }
    val location = listOfNotNull(user.province, user.district).joinToString(", ")
    var showSafetySheet by remember { mutableStateOf(false) }

    val photoHeight = (LocalConfiguration.current.screenHeightDp * 0.5f).dp

    Box(modifier = modifier.fillMaxSize().background(WickColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 148.dp),
        ) {
            WickPhotoGallery(
                photoUrls = user.photos.map { it.url },
                height = photoHeight,
                overlayContent = {
                    ProfileHeaderOverlay(displayName = user.displayName, age = age, verified = user.verificationStatus == "VERIFIED", location = location)
                },
            )

            ProfileInfoSection(bio = user.bio, interests = user.interests)
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 12.dp)
                .size(40.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background.copy(alpha = 0.6f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.profile_detail_back),
                tint = WickColors.Primary,
            )
        }

        IconButton(
            onClick = { showSafetySheet = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 12.dp, top = 12.dp)
                .size(40.dp)
                .clip(WickShapes.IconBox)
                .background(WickColors.Background.copy(alpha = 0.6f)),
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.profile_detail_more_options),
                tint = WickColors.TextPrimary,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailActionButton(
                icon = Icons.Filled.Close,
                contentDescription = stringResource(R.string.discover_pass),
                containerColor = WickColors.Surface,
                iconColor = WickColors.TextSecondary,
                size = 56.dp,
                onClick = onPass,
            )
            DetailActionButton(
                icon = Icons.Filled.LocalFireDepartment,
                contentDescription = stringResource(R.string.discover_like),
                containerColor = WickColors.Primary,
                iconColor = WickColors.OnPrimary,
                size = 76.dp,
                glow = true,
                onClick = onLike,
            )
            DetailActionButton(
                icon = Icons.Filled.Grade,
                contentDescription = stringResource(R.string.profile_detail_super_like),
                containerColor = WickColors.Tertiary,
                iconColor = WickColors.OnTertiary,
                size = 56.dp,
                glow = true,
                onClick = onSuperLike,
            )
        }
    }

    if (showSafetySheet) {
        SafetyActionSheet(
            onDismiss = { showSafetySheet = false },
            onBlock = {
                showSafetySheet = false
                onBlock()
            },
            onReport = { reason ->
                showSafetySheet = false
                onReport(reason)
            },
        )
    }
}

@Composable
private fun ProfileHeaderOverlay(displayName: String, age: Int?, verified: Boolean, location: String) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (age != null) stringResource(R.string.discover_age_location, displayName, age) else displayName,
                style = WickTextStyles.HeadlineLarge,
                color = WickColors.TextPrimary,
            )
            if (verified) {
                WickVerifiedBadge(contentDescription = stringResource(R.string.profile_detail_verified))
            }
        }

        if (location.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = WickColors.Primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(text = location, style = WickTextStyles.Body, color = WickColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(bio: String?, interests: List<String>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        bio?.takeIf { it.isNotBlank() }?.let {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WickSectionLabel(icon = Icons.AutoMirrored.Filled.Article, text = stringResource(R.string.profile_detail_about_label))
                Text(
                    text = it,
                    style = WickTextStyles.Body,
                    color = WickColors.TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WickShapes.Card)
                        .background(WickColors.Surface)
                        .padding(16.dp),
                )
            }
        }

        if (interests.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WickSectionLabel(icon = Icons.Filled.Favorite, text = stringResource(R.string.profile_detail_interests_label))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(interests, key = { it }) { interest ->
                        Text(
                            text = interest,
                            style = WickTextStyles.Label,
                            color = WickColors.Primary,
                            modifier = Modifier
                                .clip(WickShapes.Chip)
                                .background(WickColors.Primary.copy(alpha = 0.12f))
                                .border(width = 1.dp, color = WickColors.Primary.copy(alpha = 0.3f), shape = WickShapes.Chip)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    size: Dp,
    onClick: () -> Unit,
    glow: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (glow) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = WickShapes.IconBox,
                        ambientColor = containerColor,
                        spotColor = containerColor,
                    )
                } else {
                    Modifier
                },
            )
            .clip(WickShapes.IconBox)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(size / 2.2f),
        )
    }
}

