/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.cardNumberGrouping
import com.twofasapp.core.common.domain.items.formatWithGrouping
import com.twofasapp.core.common.ktx.removeWhitespace
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemImage
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewRow
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.foundation.text.secretAnnotatedString
import com.twofasapp.core.design.foundation.text.secretString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.ButtonHeight
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.feature.itemform.modals.securitytype.asTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun ItemDetailsModal(
    vaultCryptoScope: VaultCryptoScope = koinInject(),
    itemEncryptionMapper: ItemEncryptionMapper = koinInject(),
    item: Item,
    tags: List<Tag>,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit = {},
    onCopySecretFieldToClipboard: (SecretField?) -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
    ) {
        Content(
            vaultCryptoScope = vaultCryptoScope,
            itemEncryptionMapper = itemEncryptionMapper,
            item = item,
            tags = tags,
            onEditClick = onEditClick,
            onCopySecretFieldToClipboard = onCopySecretFieldToClipboard,
        )
    }
}

@Composable
private fun Content(
    vaultCryptoScope: VaultCryptoScope,
    itemEncryptionMapper: ItemEncryptionMapper,
    item: Item,
    tags: List<Tag>,
    onEditClick: () -> Unit = {},
    onCopySecretFieldToClipboard: (SecretField?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainerLow)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(bottom = ButtonHeight + 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ItemImage(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                item = item,
                size = 50.dp,
            )

            Text(
                text = item.content.name.ifEmpty { MdtLocale.strings.loginNoItemName },
                style = MdtTheme.typo.medium.lg.copy(lineHeight = 22.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
            )

            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    tags.filter { item.tagIds.contains(it.id) }.forEach { tag ->
                        TagPill(tag = tag)
                    }
                }

                Space(12.dp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedShape12)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                item.content.let { content ->
                    when (content) {
                        is ItemContent.Unknown -> Unit
                        is ItemContent.Login -> {
                            if (content.username.isNullOrEmpty().not()) {
                                Entry(
                                    title = MdtLocale.strings.loginUsername,
                                    subtitle = content.username.orEmpty(),
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.username.orEmpty()) },
                                        )
                                    },
                                )
                            }

                            content.password?.let { password ->
                                var passwordDecrypted: String? by remember { mutableStateOf(null) }

                                LifecycleResumeEffect(Unit) {
                                    onPauseOrDispose {
                                        passwordDecrypted = null
                                    }
                                }

                                Entry(
                                    title = MdtLocale.strings.loginPassword,
                                    subtitleAnnotated = passwordDecrypted?.let { passwordColorized(password = it) } ?: secretAnnotatedString(),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = passwordDecrypted != null,
                                            onToggle = {
                                                if (passwordDecrypted != null) {
                                                    passwordDecrypted = null
                                                } else {
                                                    scope.launch(Dispatchers.IO) {
                                                        vaultCryptoScope.withVaultCipher(item.vaultId) {
                                                            itemEncryptionMapper.decryptSecretField(
                                                                secretField = content.password,
                                                                securityType = item.securityType,
                                                                vaultCipher = this,
                                                            )?.let { passwordDecrypted = it }
                                                        }
                                                    }
                                                }
                                            },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.password) },
                                        )
                                    },
                                )
                            }

                            content.uris.forEachIndexed { index, uri ->
                                if (uri.text.isNotEmpty()) {
                                    Entry(
                                        title = if (content.uris.size > 1) "${MdtLocale.strings.loginUri} ${index + 1}" else MdtLocale.strings.loginUri,
                                        subtitle = uri.text,
                                        isCompact = true,
                                        maxLines = 3,
                                        actions = {
                                            IconButton(
                                                icon = MdtIcons.Open,
                                                onClick = { uriHandler.openSafely(uri.text, context) },
                                            )

                                            IconButton(
                                                icon = MdtIcons.Copy,
                                                onClick = { context.copyToClipboard(uri.text) },
                                            )
                                        },
                                    )
                                }
                            }

                            if (content.notes.isNullOrEmpty().not()) {
                                Entry(
                                    title = MdtLocale.strings.loginNotes,
                                    subtitle = content.notes.orEmpty(),
                                    isCompact = true,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                                        )
                                    },
                                )
                            }
                        }

                        is ItemContent.SecureNote -> {
                            content.text?.let {
                                var textDecrypted: String? by remember { mutableStateOf(null) }

                                LifecycleResumeEffect(Unit) {
                                    if (item.securityType == SecurityType.Tier3) {
                                        scope.launch(Dispatchers.IO) {
                                            vaultCryptoScope.withVaultCipher(item.vaultId) {
                                                itemEncryptionMapper.decryptSecretField(
                                                    secretField = content.text,
                                                    securityType = item.securityType,
                                                    vaultCipher = this,
                                                )?.let { textDecrypted = it }
                                            }
                                        }
                                    }

                                    onPauseOrDispose {
                                        textDecrypted = null
                                    }
                                }

                                Entry(
                                    title = MdtLocale.strings.secureNoteText,
                                    subtitle = textDecrypted ?: secretString(),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = {
                                                if (textDecrypted != null) {
                                                    textDecrypted = null
                                                } else {
                                                    scope.launch(Dispatchers.IO) {
                                                        vaultCryptoScope.withVaultCipher(item.vaultId) {
                                                            itemEncryptionMapper.decryptSecretField(
                                                                secretField = content.text,
                                                                securityType = item.securityType,
                                                                vaultCipher = this,
                                                            )?.let { textDecrypted = it }
                                                        }
                                                    }
                                                }
                                            },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.text) },
                                        )
                                    },
                                )
                            }

                            if (content.additionalInfo.isNullOrEmpty().not()) {
                                Entry(
                                    title = MdtLocale.strings.loginNotes,
                                    subtitle = content.additionalInfo.orEmpty(),
                                    isCompact = true,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.additionalInfo.orEmpty()) },
                                        )
                                    },
                                )
                            }
                        }

                        is ItemContent.PaymentCard -> {
                            if (content.cardHolder.isNullOrEmpty().not()) {
                                Entry(
                                    title = MdtLocale.strings.cardHolderLabel,
                                    subtitle = content.cardHolder,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.cardHolder.orEmpty()) },
                                        )
                                    },
                                )
                            }

                            content.cardNumber?.let {
                                var textDecrypted: String? by remember { mutableStateOf(null) }

                                Entry(
                                    title = MdtLocale.strings.cardNumberLabel,
                                    subtitle = textDecrypted ?: content.cardNumberMaskDisplayShort,
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = {
                                                if (textDecrypted != null) {
                                                    textDecrypted = null
                                                } else {
                                                    scope.launch(Dispatchers.IO) {
                                                        vaultCryptoScope.withVaultCipher(item.vaultId) {
                                                            itemEncryptionMapper.decryptSecretField(
                                                                secretField = content.cardNumber,
                                                                securityType = item.securityType,
                                                                vaultCipher = this,
                                                            )?.let {
                                                                textDecrypted = it.removeWhitespace().formatWithGrouping(content.cardIssuer.cardNumberGrouping())
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.cardNumber) },
                                        )
                                    },
                                )
                            }

                            content.expirationDate?.let {
                                var textDecrypted: String? by remember { mutableStateOf(null) }

                                Entry(
                                    title = MdtLocale.strings.cardExpirationDateLabel,
                                    subtitle = textDecrypted ?: secretString(count = 5),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = {
                                                if (textDecrypted != null) {
                                                    textDecrypted = null
                                                } else {
                                                    scope.launch(Dispatchers.IO) {
                                                        vaultCryptoScope.withVaultCipher(item.vaultId) {
                                                            itemEncryptionMapper.decryptSecretField(
                                                                secretField = content.expirationDate,
                                                                securityType = item.securityType,
                                                                vaultCipher = this,
                                                            )?.let { textDecrypted = it.removeWhitespace() }
                                                        }
                                                    }
                                                }
                                            },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.expirationDate) },
                                        )
                                    },
                                )
                            }

                            content.securityCode?.let {
                                var textDecrypted: String? by remember { mutableStateOf(null) }

                                Entry(
                                    title = MdtLocale.strings.cardSecurityCodeLabel,
                                    subtitle = textDecrypted ?: secretString(count = 3),
                                    actions = {
                                        SecretFieldTrailingIcon(
                                            visible = textDecrypted != null,
                                            onToggle = {
                                                if (textDecrypted != null) {
                                                    textDecrypted = null
                                                } else {
                                                    scope.launch(Dispatchers.IO) {
                                                        vaultCryptoScope.withVaultCipher(item.vaultId) {
                                                            itemEncryptionMapper.decryptSecretField(
                                                                secretField = content.securityCode,
                                                                securityType = item.securityType,
                                                                vaultCipher = this,
                                                            )?.let { textDecrypted = it.removeWhitespace() }
                                                        }
                                                    }
                                                }
                                            },
                                        )

                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { onCopySecretFieldToClipboard(content.securityCode) },
                                        )
                                    },
                                )
                            }

                            if (content.notes.isNullOrEmpty().not()) {
                                Entry(
                                    title = MdtLocale.strings.loginNotes,
                                    subtitle = content.notes.orEmpty(),
                                    isCompact = true,
                                    actions = {
                                        IconButton(
                                            icon = MdtIcons.Copy,
                                            onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                                        )
                                    },
                                )
                            }
                        }
                    }

                    Entry(
                        title = MdtLocale.strings.loginSecurityLevel,
                        subtitle = item.securityType.asTitle(),
                    )
                }
            }
        }

        Button(
            text = MdtLocale.strings.commonEdit,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            onClick = { onEditClick() },
        )
    }
}

@Composable
private fun Entry(
    title: String,
    subtitle: String? = null,
    subtitleAnnotated: AnnotatedString? = null,
    isCompact: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainerHigh)
            .padding(start = 16.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
            .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = title,
                        style = MdtTheme.typo.medium.sm,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = if (isCompact) MdtTheme.typo.regular.base.copy(lineHeight = 18.sp) else MdtTheme.typo.regular.base,
                        color = MdtTheme.color.onSurfaceVariant,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (subtitleAnnotated != null) {
                    Text(
                        text = subtitleAnnotated,
                        style = if (isCompact) MdtTheme.typo.regular.base.copy(lineHeight = 18.sp) else MdtTheme.typo.regular.base,
                        color = MdtTheme.color.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ActionsRow(useHorizontalPadding = true) {
                actions()
            }
        }
    }
}

@Composable
private fun TagPill(
    modifier: Modifier = Modifier,
    tag: Tag,
) {
    TextIcon(
        text = tag.name,
        leadingIcon = MdtIcons.Tag,
        leadingIconSize = 14.dp,
        leadingIconTint = MdtTheme.color.onSecondaryContainer,
        color = MdtTheme.color.onSecondaryContainer,
        style = MdtTheme.typo.labelSmall,
        modifier = modifier
            .clip(CircleShape)
            .background(MdtTheme.color.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun PreviewTagPill() {
    PreviewRow {
        TagPill(
            tag = Tag.Empty.copy(name = "Personal"),
        )

        TagPill(
            tag = Tag.Empty.copy(name = "Work"),
        )
    }
}