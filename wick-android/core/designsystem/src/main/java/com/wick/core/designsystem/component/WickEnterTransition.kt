package com.wick.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wick.core.designsystem.theme.WickMotion

/**
 * Fades and slides its content in once, the first time it enters composition.
 * Used for screen-level entrance animation (e.g. login/register) driven entirely
 * by the shared [WickMotion] spec.
 */
@Composable
fun WickEnterTransition(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(WickMotion.DurationEnter, easing = WickMotion.EasingStandard),
        ) + slideInVertically(
            animationSpec = tween(WickMotion.DurationEnter, easing = WickMotion.EasingStandard),
            initialOffsetY = { (it * WickMotion.EnterSlideFraction).toInt() },
        ),
    ) {
        content()
    }
}
