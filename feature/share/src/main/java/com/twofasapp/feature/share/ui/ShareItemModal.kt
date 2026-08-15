/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.share.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.showShareText
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemEntry
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.modal.ModalHeaderProperties
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ShareItemModal(
    item: Item,
    onDismissRequest: () -> Unit,
) {
    Modal(
        onDismissRequest = onDismissRequest,
        animateContentSize = true,
        headerProperties = ModalHeaderProperties(),
    ) { dismissAction ->
        ProvidesViewModelStoreOwner {
            ModalContent(
                item = item,
            )
        }
    }
}

@Composable
private fun ModalContent(
    item: Item,
    viewModel: ShareItemViewModel = koinViewModel { parametersOf(item) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ShareModalContent(
        uiState = uiState,
        onExpirationTimeSelected = viewModel::setExpirationTime,
        onOneTimeAccessToggle = viewModel::toggleOneTimeAccess,
        onPasswordChange = viewModel::setPassword,
        onContinue = { viewModel.generateLink() },
    )
}

@Composable
internal fun ShareModalContent(
    uiState: ShareItemUiState,
    onExpirationTimeSelected: (ExpirationTime) -> Unit = {},
    onOneTimeAccessToggle: () -> Unit = {},
    onPasswordChange: (String?) -> Unit = {},
    onContinue: () -> Unit = {},
) {
    val shareColor = Color(0xFF8800FF)
    val strings = MdtLocale.strings

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                            center = Offset(size.width / 2f, -size.width * 0.2f),
                            radius = size.width * 0.7f,
                        ),
                        center = Offset(size.width / 2f, -size.width * 0.2f),
                        radius = size.width * 0.7f,
                    )
                },
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Space(16.dp)

            Text(
                text = strings.shareLinkItemTitle,
                style = MdtTheme.typo.semiBold.xl,
                color = MdtTheme.color.onSurface,
            )

            Space(24.dp)

            ItemEntry(
                item = uiState.item,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .clip(RoundedShape12)
                    .background(MdtTheme.color.surfaceContainerLow.copy(alpha = 0.9f))
                    .border(
                        width = 1.dp,
                        shape = RoundedShape12,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                shareColor.copy(alpha = 0.3f),
                                shareColor.copy(alpha = 0.5f),
                                shareColor.copy(alpha = 0.3f),
                            ),
                        ),
                    )
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
            )

            when (uiState.screenState) {
                ShareItemScreenState.Form -> {
                    ShareFormContent(
                        uiState = uiState,
                        onExpirationTimeSelected = onExpirationTimeSelected,
                        onOneTimeAccessToggle = onOneTimeAccessToggle,
                        onPasswordChange = onPasswordChange,
                        onContinue = onContinue,
                    )
                }

                ShareItemScreenState.Loading -> {
                    ShareLoadingContent()
                }

                ShareItemScreenState.Success -> {
                    ShareSuccessContent(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ShareFormContent(
    uiState: ShareItemUiState,
    onExpirationTimeSelected: (ExpirationTime) -> Unit,
    onOneTimeAccessToggle: () -> Unit,
    onPasswordChange: (String?) -> Unit,
    onContinue: () -> Unit,
) {
    val strings = MdtLocale.strings
    var showExpirationTimePopup by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    Space(32.dp)

    Text(
        text = strings.shareLinkItemShareSettings,
        style = MdtTheme.typo.semiBold.sm,
        color = MdtTheme.color.primary,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )

    Space(12.dp)

    OptionEntry(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainerHigh),
        title = strings.shareLinkItemExpirationTime,
        subtitle = uiState.expirationTime.label,
        subtitleColor = MdtTheme.color.primary,
        icon = MdtIcons.Timer,
        iconTint = MdtTheme.color.onSurface,
        content = {
            DropdownMenu(
                modifier = Modifier.widthIn(min = 180.dp),
                visible = showExpirationTimePopup,
                onDismissRequest = { showExpirationTimePopup = false },
                anchor = {
                    Icon(
                        painter = MdtIcons.ChevronDown,
                        contentDescription = null,
                    )
                },
                content = {
                    listOf(
                        listOf(ExpirationTime.Min5, ExpirationTime.Min30, ExpirationTime.Hour1),
                        listOf(ExpirationTime.Day1, ExpirationTime.Days7, ExpirationTime.Days30),
                    ).forEachIndexed { index, group ->
                        group.forEach { time ->
                            DropdownMenuItem(
                                text = time.label,
                                leadingIcon = if (uiState.expirationTime == time) MdtIcons.CircleCheckFilled else MdtIcons.CircleUncheck,
                                onClick = {
                                    showExpirationTimePopup = false
                                    onExpirationTimeSelected(time)
                                },
                            )
                        }

                        if (index == 0) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        }
                    }
                },
            )
        },
        onClick = { showExpirationTimePopup = true },
    )

    Space(12.dp)

    OptionSwitch(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainerHigh),
        checked = uiState.oneTimeAccess,
        title = strings.shareLinkItemOneTimeAccess,
        subtitle = strings.shareLinkItemOneTimeAccessDescription,
        icon = MdtIcons.Refresh,
        iconTint = MdtTheme.color.onSurface,
        testTag = "shareOneTimeAccessSwitch",
        onToggle = { onOneTimeAccessToggle() },
    )

    Space(12.dp)

    OptionEntry(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainerHigh),
        title = strings.shareLinkItemAccessPassword,
        subtitle = if (uiState.password.isNullOrBlank()) {
            strings.shareLinkItemAccessPasswordDescription
        } else {
            uiState.password.map { "•" }.joinToString("")
        },
        icon = MdtIcons.Lock,
        iconTint = MdtTheme.color.onSurface,
        content = {
            Icon(
                painter = MdtIcons.ChevronRight,
                contentDescription = null,
            )
        },
        onClick = { showPasswordDialog = true },
    )

    if (showPasswordDialog) {
        SharePasswordDialog(
            mode = SharePasswordDialogMode.SetPassword,
            prefillPassword = uiState.password,
            onDismissRequest = { showPasswordDialog = false },
            onSubmit = { onPasswordChange(it) },
            onRemove = { onPasswordChange(null) },
        )
    }

    Space(16.dp)

    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        text = strings.commonContinue,
        onClick = { onContinue() },
    )

    Space(16.dp)
}

@Composable
private fun ColumnScope.ShareLoadingContent() {
    Space(32.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MdtTheme.color.surfaceContainerHigh, shape = RoundedShape12)
            .padding(horizontal = 16.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgress(
            modifier = Modifier.size(32.dp),
        )

        Space(16.dp)

        Text(
            text = MdtLocale.strings.shareLinkItemGeneratingLink,
            style = MdtTheme.typo.medium.sm,
            color = MdtTheme.color.primary,
        )
    }

    Space(24.dp)
}

@Composable
private fun ColumnScope.ShareSuccessContent(
    uiState: ShareItemUiState,
) {
    val context = LocalContext.current
    val strings = MdtLocale.strings

    Space(32.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(color = MdtTheme.color.surfaceContainerHigh, shape = RoundedShape12),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = MdtIcons.Check,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MdtTheme.color.success.copy(alpha = 0.2f))
                    .padding(4.dp),
                tint = MdtTheme.color.success,
            )

            Space(16.dp)

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = strings.shareLinkItemLinkGenerated,
                    style = MdtTheme.typo.semiBold.lg,
                    color = MdtTheme.color.onSurface,
                    modifier = Modifier.offset(x = 1.dp),
                )

                Space(6.dp)

                TextIcon(
                    text = strings.shareLinkItemExpiresIn.format(uiState.expirationTime.label),
                    leadingIcon = MdtIcons.Timer,
                    leadingIconSize = 12.dp,
                    leadingIconTint = MdtTheme.color.onSurfaceVariant,
                    leadingIconSpacer = 3.dp,
                    style = MdtTheme.typo.regular.xs,
                    color = MdtTheme.color.onSurfaceVariant,
                )

                if (uiState.oneTimeAccess) {
                    Space(3.dp)

                    TextIcon(
                        text = strings.shareLinkItemOneTimeAccess,
                        leadingIcon = MdtIcons.Refresh,
                        leadingIconSize = 12.dp,
                        leadingIconTint = MdtTheme.color.onSurfaceVariant,
                        leadingIconSpacer = 3.dp,
                        style = MdtTheme.typo.regular.xs,
                        color = MdtTheme.color.onSurfaceVariant,
                    )
                }
            }

            ActionsRow {
                IconButton(
                    modifier = Modifier
                        .offset(x = 4.dp)
                        .testTag("shareLinkCopyButton"),
                    iconSize = 20.dp,
                    icon = MdtIcons.Copy,
                    onClick = { context.copyToClipboard(uiState.link) },
                )
            }
        }

        HorizontalDivider(
            color = MdtTheme.color.outlineVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp),
        ) {
            Text(
                text = strings.shareLinkItemLinkLabel,
                style = MdtTheme.typo.regular.xs,
                color = MdtTheme.color.onSurfaceVariant,
            )

            Space(4.dp)

            Text(
                text = uiState.link,
                overflow = TextOverflow.MiddleEllipsis,
                style = MdtTheme.typo.regular.sm,
                maxLines = 1,
                color = MdtTheme.color.primary,
            )
        }
    }

    if (uiState.password.isNullOrBlank().not()) {
        Space(12.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(MdtTheme.color.surfaceContainerHigh, shape = RoundedShape12)
                .padding(vertical = 16.dp)
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = strings.commonPassword,
                    style = MdtTheme.typo.regular.xs,
                    color = MdtTheme.color.onSurfaceVariant,
                )

                Space(4.dp)

                Text(
                    text = uiState.password.map { "•" }.joinToString(""),
                    style = MdtTheme.typo.regular.sm,
                    color = MdtTheme.color.primary,
                )
            }

            Space(4.dp)

            ActionsRow {
                IconButton(
                    modifier = Modifier.testTag("sharePasswordCopyButton"),
                    iconSize = 20.dp,
                    icon = MdtIcons.Copy,
                    onClick = { context.copyToClipboard(text = uiState.password, isSensitive = true) },
                )
            }
        }
    }

    Space(16.dp)

    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        text = strings.itemViewActionShareLink,
        onClick = { context.showShareText(uiState.link) },
    )

    Space(16.dp)
}

@Preview
@Composable
private fun PreviewDefault() {
    PreviewTheme {
        Box(
            modifier = Modifier
                .background(MdtTheme.color.surfaceContainerLow),
        ) {
            ShareModalContent(
                uiState = ShareItemUiState(
                    item = Item.create(
                        ItemContentType.SecureNote,
                        ItemContent.SecureNote.Empty.copy(name = "Preview Note"),
                    ),
                    screenState = ShareItemScreenState.Form,
                    expirationTime = ExpirationTime.Min30,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSuccess() {
    PreviewTheme {
        Box(
            modifier = Modifier
                .background(MdtTheme.color.surfaceContainerLow),
        ) {
            ShareModalContent(
                uiState = ShareItemUiState(
                    item = Item.create(
                        ItemContentType.SecureNote,
                        ItemContent.SecureNote.Empty.copy(name = "Preview Note"),
                    ),
                    screenState = ShareItemScreenState.Success,
                    expirationTime = ExpirationTime.Min30,
                    link = "https://share.2fas.com/2m6kNgsy9XePkhuwjAsdudiTlx72LPfSmFd2uwt34LU",
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    PreviewTheme {
        Box(
            modifier = Modifier
                .background(MdtTheme.color.surfaceContainerLow),
        ) {
            ShareModalContent(
                uiState = ShareItemUiState(
                    item = Item.create(
                        ItemContentType.SecureNote,
                        ItemContent.SecureNote.Empty.copy(name = "Preview Note"),
                    ),
                    screenState = ShareItemScreenState.Loading,
                    expirationTime = ExpirationTime.Min30,
                ),
            )
        }
    }
}