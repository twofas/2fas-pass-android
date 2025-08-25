/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.keyboardAsState
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.PasswordGenerator
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.ktx.formatDateTime
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.login.LoginImage
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.Password
import com.twofasapp.core.design.foundation.textfield.PasswordTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.foundation.textfield.passwordColors
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.loginform.ui.composables.PasswordSuggestionsBar
import com.twofasapp.feature.loginform.ui.composables.SecurityTypePicker
import com.twofasapp.feature.loginform.ui.composables.TagsPicker
import com.twofasapp.feature.loginform.ui.composables.UriField
import com.twofasapp.feature.loginform.ui.composables.UsernameSuggestionsBar
import com.twofasapp.feature.loginform.ui.modal.ChangeIconModal
import com.twofasapp.feature.loginform.ui.modal.PasswordGeneratorModal
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    initialLogin: Login,
    containerColor: Color = MdtTheme.color.background,
    confirmUnsavedChanges: Boolean = true,
    onLoginUpdated: (Login) -> Unit = {},
    onIsValidUpdated: (Boolean) -> Unit = {},
    onHasUnsavedChangesUpdated: (Boolean) -> Unit = {},
    onCloseWithoutSaving: () -> Unit = {},
) {
    LoginFormInternal(
        modifier = modifier,
        initialLogin = initialLogin,
        containerColor = containerColor,
        confirmUnsavedChanges = confirmUnsavedChanges,
        onLoginUpdated = onLoginUpdated,
        onIsValidUpdated = onIsValidUpdated,
        onHasUnsavedChangesUpdated = onHasUnsavedChangesUpdated,
        onCloseWithoutSaving = onCloseWithoutSaving,
    )
}

@Composable
private fun LoginFormInternal(
    modifier: Modifier = Modifier,
    viewModel: LoginFormViewModel = koinViewModel(),
    initialLogin: Login,
    containerColor: Color,
    confirmUnsavedChanges: Boolean = true,
    onLoginUpdated: (Login) -> Unit = {},
    onIsValidUpdated: (Boolean) -> Unit = {},
    onHasUnsavedChangesUpdated: (Boolean) -> Unit = {},
    onCloseWithoutSaving: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialLogin) {
        viewModel.initLogin(initialLogin)
    }

    if (uiState.initialised) {
        LaunchedEffect(uiState.login) {
            onLoginUpdated(uiState.login)
        }

        LaunchedEffect(uiState.valid) {
            onIsValidUpdated(uiState.valid)
        }

        LaunchedEffect(uiState.hasUnsavedChanges) {
            onHasUnsavedChangesUpdated(uiState.hasUnsavedChanges)
        }

        Content(
            modifier = modifier,
            uiState = uiState,
            containerColor = containerColor,
            confirmUnsavedChanges = confirmUnsavedChanges,
            onNameChange = { viewModel.updateName(it) },
            onUsernameChange = { viewModel.updateUsername(it) },
            onPasswordChange = { viewModel.updatePassword(it) },
            onPasswordSettingsChange = { viewModel.updatePasswordSettings(it) },
            onIconTypeChange = { viewModel.updateIconType(it) },
            onIconUriChange = { viewModel.updateIconUriIndex(it) },
            onLabelTextChange = { viewModel.updateLabelText(it) },
            onLabelColorChange = { viewModel.updateLabelColor(it) },
            onImageUrlChange = { viewModel.updateImageUrl(it) },
            onUriChange = { index, uri -> viewModel.updateUri(index, uri) },
            onSecurityLevelChange = { viewModel.updateSecurityLevel(it) },
            onTagsChange = { viewModel.updateTags(it) },
            onNotesChange = { viewModel.updateNotes(it) },
            onAddUriClick = { viewModel.addUri() },
            onDeleteUriClick = { viewModel.deleteUri(it) },
            onCloseWithoutSaving = onCloseWithoutSaving,
        )
    }
}

@Composable
private fun Content(
    modifier: Modifier,
    uiState: LoginFormUiState,
    containerColor: Color,
    confirmUnsavedChanges: Boolean = true,
    onNameChange: (String) -> Unit = {},
    onUsernameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onPasswordSettingsChange: (PasswordGeneratorSettings) -> Unit = {},
    onIconTypeChange: (IconType) -> Unit = {},
    onIconUriChange: (Int?) -> Unit = {},
    onLabelTextChange: (String?) -> Unit = {},
    onLabelColorChange: (String?) -> Unit = {},
    onImageUrlChange: (String?) -> Unit = {},
    onUriChange: (Int, LoginUri) -> Unit = { _, _ -> },
    onSecurityLevelChange: (SecurityType) -> Unit = {},
    onTagsChange: (List<String>) -> Unit = {},
    onNotesChange: (String) -> Unit = {},
    onAddUriClick: () -> Unit = {},
    onDeleteUriClick: (Int) -> Unit = {},
    onCloseWithoutSaving: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val strings = MdtLocale.strings
    var passwordVisible by remember { mutableStateOf(false) }
    var usernameTextValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    var passwordTextValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    var usernameFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var showPasswordGeneratorModal by remember { mutableStateOf(false) }
    var showChangeIconModal by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    val isKeyboardOpened by keyboardAsState()

    BackHandler(enabled = uiState.hasUnsavedChanges && confirmUnsavedChanges) {
        focusManager.clearFocus()
        showUnsavedChangesDialog = true
    }

    LaunchedEffect(uiState.login.id) {
        usernameTextValue = usernameTextValue.copy(text = uiState.login.username.orEmpty())
        passwordTextValue = passwordTextValue.copy(text = (uiState.login.password as? SecretField.Visible)?.value.orEmpty())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(containerColor),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding, top = 8.dp),
        ) {
            listItem(LoginFormListItem.Field("Name")) {
                TextField(
                    value = uiState.login.name,
                    onValueChange = onNameChange,
                    labelText = strings.loginName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    trailingIcon = {
                        LoginImage(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedShape12)
                                .clickable {
                                    focusManager.clearFocus()
                                    showChangeIconModal = true
                                },
                            iconType = uiState.login.iconType,
                            iconUrl = uiState.login.iconUrl,
                            labelText = uiState.login.labelText ?: uiState.login.defaultLabelText,
                            labelColor = uiState.login.labelColor,
                            customImageUrl = uiState.login.customImageUrl,
                            size = 42.dp,
                        )
                    },
                )
            }

            listItem(LoginFormListItem.Field("Username")) {
                TextField(
                    value = usernameTextValue,
                    onValueChange = {
                        onUsernameChange(it.text)
                        usernameTextValue = it
                    },
                    labelText = strings.loginUsername,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .onFocusChanged { usernameFocused = it.isFocused },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
            }

            listItem(LoginFormListItem.Field("Password")) {
                TextField(
                    value = passwordTextValue,
                    onValueChange = {
                        onPasswordChange(it.text)
                        passwordTextValue = it
                    },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    labelText = strings.loginPassword,
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .onFocusChanged { passwordFocused = it.isFocused },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    visualTransformation = VisualTransformation.Password(passwordVisible, passwordColors),
                    trailingIcon = {
                        ActionsRow(
                            useHorizontalPadding = true,
                        ) {
                            PasswordTrailingIcon(
                                passwordVisible = passwordVisible,
                                onToggle = { passwordVisible = passwordVisible.not() },
                            )
                        }
                    },
                )
            }

            uiState.login.uris.forEachIndexed { index, uri ->
                listItem(LoginFormListItem.Field("Uri$index")) {
                    UriField(
                        modifier = Modifier.animateItem(),
                        index = index,
                        loginUri = uri,
                        totalCount = uiState.login.uris.size,
                        onUriChange = onUriChange,
                        onDeleteUri = onDeleteUriClick,
                    )
                }
            }

            listItem(LoginFormListItem.AddUri) {
                Button(
                    modifier = Modifier
                        .offset(y = (-4).dp)
                        .animateItem(),
                    style = ButtonStyle.Text,
                    text = strings.loginAddUri,
                    onClick = {
                        onAddUriClick()
                        focusManager.clearFocus()
                    },
                )
            }

            listItem(LoginFormListItem.SecurityType) {
                SecurityTypePicker(
                    modifier = Modifier.animateItem(),
                    securityType = uiState.login.securityType,
                    onSelect = onSecurityLevelChange,
                    onOpened = { focusManager.clearFocus() },
                )
            }

            listItem(LoginFormListItem.Tags) {
                TagsPicker(
                    modifier = Modifier.animateItem(),
                    tags = uiState.tags,
                    selectedTagIds = uiState.login.tagIds,
                    onOpened = { focusManager.clearFocus() },
                    onConfirmTagsSelections = onTagsChange,
                )
            }

            listItem(LoginFormListItem.Field("Notes")) {
                TextField(
                    value = uiState.login.notes.orEmpty(),
                    onValueChange = { onNotesChange(it) },
                    labelText = strings.loginNotes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    minLines = 3,
                    maxLines = 3,
                    supportingText = if (uiState.login.notes.orEmpty().length > 2048) "Notes can not be longer than 2048 characters" else null,
                    isError = uiState.login.notes.orEmpty().length > 2048,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                )
            }

            if (uiState.login.id.isNotEmpty()) {
                listItem(LoginFormListItem.Info) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        text = buildString {
                            append(" Created at: ")
                            append(uiState.login.createdAt.formatDateTime())
                            appendLine()
                            append(" Updated at: ")
                            append(uiState.login.updatedAt.formatDateTime())
                        },
                        style = MdtTheme.typo.bodySmall,
                        color = MdtTheme.color.onSurface28,
                    )
                }
            }
        }

        AnimatedVisibility(usernameFocused && isKeyboardOpened) {
            UsernameSuggestionsBar(
                modifier = Modifier.fillMaxWidth(),
                usernameSuggestions = uiState.usernameSuggestionsFiltered,
                onUsernameClick = {
                    usernameTextValue = usernameTextValue.copy(text = it, selection = TextRange(it.length))
                    onUsernameChange(it)
                },
            )
        }

        AnimatedVisibility(passwordFocused && isKeyboardOpened) {
            PasswordSuggestionsBar(
                modifier = Modifier.fillMaxWidth(),
                onGenerateClick = {
                    val password = PasswordGenerator.generatePassword(
                        settings = uiState.passwordGeneratorSettings,
                    )

                    passwordTextValue = passwordTextValue.copy(text = password, selection = TextRange(password.length))
                    onPasswordChange(password)
                },
                onOpenGeneratorClick = {
                    focusManager.clearFocus()
                    showPasswordGeneratorModal = true
                },
            )
        }
    }

    if (showPasswordGeneratorModal) {
        PasswordGeneratorModal(
            onDismissRequest = { showPasswordGeneratorModal = false },
            settings = uiState.passwordGeneratorSettings,
            onUsePasswordClick = { password, settings ->
                passwordTextValue = passwordTextValue.copy(text = password, selection = TextRange(password.length))
                onPasswordChange(password)
                onPasswordSettingsChange(settings)
            },
        )
    }

    if (showChangeIconModal) {
        ChangeIconModal(
            onDismissRequest = { showChangeIconModal = false },
            login = uiState.login,
            onIconTypeChange = onIconTypeChange,
            onIconUriIndexChange = onIconUriChange,
            onLabelTextChange = onLabelTextChange,
            onLabelColorChange = onLabelColorChange,
            onImageUrlChange = onImageUrlChange,
        )
    }

    if (showUnsavedChangesDialog) {
        ConfirmDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            onPositive = onCloseWithoutSaving,
            icon = MdtIcons.Warning,
            title = strings.loginUnsavedChangesDialogTitle,
            body = strings.loginUnsavedChangesDialogDescription,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(appTheme = AppTheme.Dark) {
        Content(
            modifier = Modifier,
            uiState = LoginFormUiState(login = Login.Preview),
            containerColor = MdtTheme.color.background,
        )
    }
}