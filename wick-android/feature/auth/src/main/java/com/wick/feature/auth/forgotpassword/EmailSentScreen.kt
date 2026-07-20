package com.wick.feature.auth.forgotpassword

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.wick.core.designsystem.component.WickButton
import com.wick.core.designsystem.component.WickEnterTransition
import com.wick.core.designsystem.component.WickTopBar
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles
import com.wick.feature.auth.R

@Composable
fun EmailSentRoute(email: String, onBack: () -> Unit) {
    EmailSentScreen(email = email, onBack = onBack)
}

@Composable
private fun EmailSentScreen(email: String, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        WickTopBar(onBackClick = onBack)

        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            WickEnterTransition {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = WickShapes.Card,
                                ambientColor = WickColors.Primary,
                                spotColor = WickColors.Primary,
                            )
                            .clip(WickShapes.Card)
                            .background(WickColors.Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = WickColors.OnPrimary,
                            modifier = Modifier.size(40.dp),
                        )
                    }

                    Text(
                        text = stringResource(R.string.email_sent_title),
                        style = WickTextStyles.HeadlineLarge,
                        color = WickColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = WickColors.TextPrimary, fontWeight = FontWeight.Bold)) {
                                append(email)
                            }
                            append(" " + stringResource(R.string.email_sent_body))
                        },
                        style = WickTextStyles.Body,
                        color = WickColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    WickButton(
                        text = stringResource(R.string.email_sent_open_mail_app),
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_EMAIL),
                                )
                            } catch (error: ActivityNotFoundException) {
                            }
                        },
                        modifier = Modifier.shadow(
                            elevation = 12.dp,
                            shape = WickShapes.Button,
                            ambientColor = WickColors.Primary,
                            spotColor = WickColors.Primary,
                        ),
                    )

                    Text(
                        text = stringResource(R.string.email_sent_resend),
                        style = WickTextStyles.Label,
                        color = WickColors.TextSecondary,
                        modifier = Modifier.clickable { },
                    )
                }
            }
        }
    }
}
