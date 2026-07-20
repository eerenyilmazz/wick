package com.wick.feature.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wick.core.designsystem.component.WickAsyncImage
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickButtonVariant
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun MatchDialog(
    displayName: String,
    ownPhotoUrl: String?,
    matchedPhotoUrl: String?,
    onSendInvite: () -> Unit,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(280)) + scaleIn(tween(280), initialScale = 0.8f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(WickShapes.Card)
                    .background(WickColors.SurfaceElevated)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MatchAvatar(
                        photoUrl = ownPhotoUrl,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = (-52).dp),
                    )
                    MatchAvatar(
                        photoUrl = matchedPhotoUrl,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 52.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                            .clip(WickShapes.IconBox)
                            .background(WickColors.Primary)
                            .border(2.dp, WickColors.SurfaceElevated, WickShapes.IconBox),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = WickColors.OnPrimary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.discover_match_title),
                    style = WickTextStyles.HeadlineLarge,
                    color = WickColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.discover_match_body, displayName),
                    style = WickTextStyles.Body,
                    color = WickColors.TextSecondary,
                )
                WickButton(
                    text = stringResource(R.string.discover_match_send_invite),
                    onClick = onSendInvite,
                    variant = WickButtonVariant.Primary,
                )
                WickButton(
                    text = stringResource(R.string.discover_match_dismiss),
                    onClick = onDismiss,
                    variant = WickButtonVariant.Secondary,
                )
            }
        }
    }
}

@Composable
private fun MatchAvatar(photoUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(WickShapes.IconBox)
            .background(WickColors.Surface)
            .border(3.dp, WickColors.SurfaceElevated, WickShapes.IconBox),
    ) {
        if (photoUrl != null) {
            WickAsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(WickShapes.IconBox),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = WickColors.TextSecondary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }
    }
}
