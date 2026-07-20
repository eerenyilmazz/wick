package com.wick.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.wick.core.designsystem.R
import com.wick.core.designsystem.theme.WickColors
import com.wick.core.designsystem.theme.WickShapes
import com.wick.core.designsystem.theme.WickTextStyles

@Composable
fun WickTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = WickTextStyles.Body, color = WickColors.TextMuted) },
        textStyle = WickTextStyles.Body,
        singleLine = singleLine,
        isError = isError,
        shape = WickShapes.IconBox,
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = wickFilledTextFieldColors(),
    )
}

@Composable
fun WickPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, style = WickTextStyles.Body, color = WickColors.TextMuted) },
        textStyle = WickTextStyles.Body,
        singleLine = true,
        isError = isError,
        shape = WickShapes.IconBox,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) stringResource(R.string.password_field_hide) else stringResource(R.string.password_field_show),
                    tint = WickColors.Primary,
                )
            }
        },
        colors = wickFilledTextFieldColors(),
    )
}

@Composable
private fun wickFilledTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = WickColors.Surface,
    unfocusedContainerColor = WickColors.Surface,
    errorContainerColor = WickColors.Surface,
    disabledContainerColor = WickColors.Surface,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = WickColors.Urgency,
    focusedTextColor = WickColors.TextPrimary,
    unfocusedTextColor = WickColors.TextPrimary,
    cursorColor = WickColors.Primary,
)
