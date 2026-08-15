/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.SecretField
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.theme.DialogShape
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.share.R
import kotlinx.coroutines.android.awaitFrame

internal enum class SharePasswordDialogMode {
    SetPassword,
    EnterPassword,
}

@Composable
internal fun SharePasswordDialog(
    onDismissRequest: () -> Unit,
    mode: SharePasswordDialogMode = SharePasswordDialogMode.SetPassword,
    prefillPassword: String? = null,
    errorText: String? = null,
    loading: Boolean = false,
    dismissOnSubmit: Boolean = true,
    onSubmit: (String) -> Unit,
    onRemove: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SharePasswordDialogContent(
            mode = mode,
            prefillPassword = prefillPassword,
            errorText = errorText,
            loading = loading,
            onCancel = onDismissRequest,
            onSubmit = {
                onSubmit(it)
                if (dismissOnSubmit) onDismissRequest()
            },
            onRemove = {
                onRemove()
                onDismissRequest()
            },
        )
    }
}

@Composable
private fun SharePasswordDialogContent(
    mode: SharePasswordDialogMode,
    prefillPassword: String?,
    errorText: String? = null,
    loading: Boolean = false,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val shareColor = Color(0xFF8800FF)
    val strings = MdtLocale.strings
    val initialPassword = remember { prefillPassword.orEmpty() }
    val focusRequester = remember { FocusRequester() }
    var secretVisible by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialPassword,
                selection = TextRange(initialPassword.length),
            ),
        )
    }
    var startedTyping by remember { mutableStateOf(false) }

    // Snapshot the input value at the moment an external error is surfaced (e.g. wrong password).
    // We show the error until the user modifies the input, then suppress it and re-enable submit.
    var errorInputSnapshot by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(errorText) {
        errorInputSnapshot = if (errorText != null) textFieldValue.text else null
    }

    val isValid = textFieldValue.text.length >= MinPasswordLength
    val isChanged = textFieldValue.text != initialPassword
    val showExternalError = errorText != null && errorInputSnapshot == textFieldValue.text
    val showLocalError = startedTyping && textFieldValue.text.isNotEmpty() && !isValid
    val showError = showExternalError || showLocalError
    val supportingMessage = when {
        showExternalError -> errorText
        showLocalError -> strings.shareLinkItemPasswordMinLength.format(MinPasswordLength)
        else -> " "
    }
    val submitEnabled = if (loading) {
        false
    } else {
        when (mode) {
            SharePasswordDialogMode.SetPassword -> isValid && isChanged
            SharePasswordDialogMode.EnterPassword -> isValid
        }
    }

    val title = when (mode) {
        SharePasswordDialogMode.SetPassword -> strings.shareLinkItemSetPasswordTitle
        SharePasswordDialogMode.EnterPassword -> strings.shareLinkImportPasswordTitle
    }
    val subtitle = when (mode) {
        SharePasswordDialogMode.SetPassword -> strings.shareLinkItemSetPasswordSubtitle
        SharePasswordDialogMode.EnterPassword -> strings.shareLinkImportPasswordSubtitle
    }
    val submitText = when (mode) {
        SharePasswordDialogMode.SetPassword -> strings.commonSave
        SharePasswordDialogMode.EnterPassword -> strings.shareLinkImportUnlock
    }

    LaunchedEffect(Unit) {
        awaitFrame()
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .sizeIn(minWidth = 280.dp, maxWidth = 560.dp),
        shape = DialogShape,
        color = MdtTheme.color.surfaceContainerLow,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    shareColor.copy(alpha = 0.6f),
                                    shareColor.copy(alpha = 0.4f),
                                    shareColor.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width / 2f, -size.width * 0.3f),
                                radius = size.width * 0.9f,
                            ),
                            center = Offset(size.width / 2f, -size.width * 0.3f),
                            radius = size.width * 0.9f,
                        )
                    },
            )

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .testTag("sharePasswordCloseButton"),
                icon = MdtIcons.Close,
                onClick = onCancel,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Space(48.dp)

                Icon(
                    painter = painterResource(R.drawable.share_password_icon),
                    tint = Color(0xFF8800FF),
                    modifier = Modifier.size(52.dp),
                    contentDescription = null,
                )

                Space(24.dp)

                Text(
                    text = title,
                    style = MdtTheme.typo.semiBold.xl2,
                    color = MdtTheme.color.onSurface,
                    textAlign = TextAlign.Center,
                )

                Space(8.dp)

                Text(
                    text = subtitle,
                    style = MdtTheme.typo.regular.base,
                    color = MdtTheme.color.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                Space(32.dp)

                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        startedTyping = true
                        textFieldValue = it
                    },
                    labelText = strings.commonPassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    maxLines = 1,
                    isError = showError,
                    supportingText = supportingMessage,
                    visualTransformation = VisualTransformation.SecretField(secretVisible),
                    trailingIcon = {
                        SecretFieldTrailingIcon(
                            visible = secretVisible,
                            onToggle = { secretVisible = !secretVisible },
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (submitEnabled) onSubmit(textFieldValue.text.trim())
                        },
                    ),
                )

                Space(8.dp)

                if (mode == SharePasswordDialogMode.SetPassword && initialPassword.isNotEmpty()) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        text = strings.shareLinkItemRemovePassword,
                        style = ButtonStyle.Text,
                        onClick = onRemove,
                    )

                    Space(8.dp)
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    text = submitText,
                    enabled = submitEnabled,
                    loading = loading,
                    onClick = { onSubmit(textFieldValue.text.trim()) },
                )

                Space(24.dp)
            }
        }
    }
}

private const val MinPasswordLength = 8

@Preview
@Composable
private fun PreviewSetEmpty() {
    PreviewTheme {
        SharePasswordDialogContent(
            mode = SharePasswordDialogMode.SetPassword,
            prefillPassword = null,
            onCancel = {},
            onSubmit = {},
            onRemove = {},
        )
    }
}

@Preview
@Composable
private fun PreviewSetPrefilled() {
    PreviewTheme {
        SharePasswordDialogContent(
            mode = SharePasswordDialogMode.SetPassword,
            prefillPassword = "supersecret",
            onCancel = {},
            onSubmit = {},
            onRemove = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEnter() {
    PreviewTheme {
        SharePasswordDialogContent(
            mode = SharePasswordDialogMode.EnterPassword,
            prefillPassword = null,
            onCancel = {},
            onSubmit = {},
            onRemove = {},
        )
    }
}