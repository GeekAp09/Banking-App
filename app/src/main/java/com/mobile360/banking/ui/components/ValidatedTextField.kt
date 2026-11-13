package com.mobile360.banking.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/** Error text wins over helper text; null when neither is set. */
fun validatedFieldMessage(error: String?, helper: String?): String? = error ?: helper

/** The eye toggle's content description for the current visibility. */
fun eyeToggleDescription(visible: Boolean): String = if (visible) "Hide code" else "Show code"

/** Secret values are masked unless the user chose to reveal them. */
fun fieldTransformation(secret: Boolean, visible: Boolean): VisualTransformation =
    if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None

/**
 * Shared text input: error border, helper or error line (always text, not colour alone)
 * and an optional eye toggle for secret values (masked by default).
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    helper: String? = null,
    secret: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val message = validatedFieldMessage(error, helper)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        isError = error != null,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = fieldTransformation(secret, visible),
        supportingText = message?.let { { Text(it) } },
        trailingIcon = if (secret) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = eyeToggleDescription(visible),
                    )
                }
            }
        } else null,
    )
}
