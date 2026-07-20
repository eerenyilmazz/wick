package com.wick.feature.profile.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wick.core.designsystem.component.WickPhotoGallery
import com.wick.core.designsystem.component.WickSectionLabel
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.core.model.User
import com.wick.feature.profile.R
import java.time.LocalDate
import java.time.Period

@Composable
fun ProfilePreviewRoute(
    onBack: () -> Unit,
    viewModel: ProfilePreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        val user = uiState.user
        when {
            uiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = WickColors.Primary,
            )

            user != null -> ProfilePreviewContent(user = user, onBack = onBack)
        }
    }
}

@Composable
private fun ProfilePreviewContent(user: User, onBack: () -> Unit) {
    val age = user.dateOfBirth?.let { rawDate ->
        runCatching { Period.between(LocalDate.parse(rawDate), LocalDate.now()).years }.getOrNull()
    }
    val location = listOfNotNull(user.province, user.district).joinToString(", ")
    val photoHeight = (LocalConfiguration.current.screenHeightDp * 0.5f).dp

    Box(modifier = Modifier.fillMaxSize().background(WickColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            WickPhotoGallery(
                photoUrls = user.photos.map { it.url },
                height = photoHeight,
                overlayContent = {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (age != null) {
                                    stringResource(R.string.profile_preview_age_name, user.displayName, age)
                                } else {
                                    user.displayName
                                },
                                style = WickTextStyles.HeadlineLarge,
                                color = WickColors.TextPrimary,
                            )
                            if (user.verified) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(WickShapes.IconBox)
                                        .background(WickColors.Primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = stringResource(R.string.profile_preview_verified),
                                        tint = WickColors.OnPrimary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }

                        if (location.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
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
                },
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WickSectionLabel(
                            icon = Icons.AutoMirrored.Filled.Article,
                            text = stringResource(R.string.profile_preview_about_label),
                        )
                        Text(
                            text = bio,
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

                if (user.interests.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WickSectionLabel(
                            icon = Icons.Filled.Favorite,
                            text = stringResource(R.string.profile_preview_interests_label),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(user.interests, key = { it }) { interest ->
                                Text(
                                    text = interest,
                                    style = WickTextStyles.Label,
                                    color = WickColors.Primary,
                                    modifier = Modifier
                                        .clip(WickShapes.Chip)
                                        .background(WickColors.Primary.copy(alpha = 0.12f))
                                        .border(
                                            width = 1.dp,
                                            color = WickColors.Primary.copy(alpha = 0.3f),
                                            shape = WickShapes.Chip,
                                        )
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
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
                contentDescription = stringResource(com.wick.core.designsystem.R.string.wick_back),
                tint = WickColors.Primary,
            )
        }
    }
}
