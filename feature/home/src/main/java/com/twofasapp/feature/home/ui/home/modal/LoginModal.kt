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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.login.LoginImage
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.textfield.PasswordTrailingIcon
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.ButtonHeight
import com.twofasapp.core.design.theme.RoundedShapeIndexed
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.feature.loginform.ui.modal.asTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun LoginModal(
    vaultCryptoScope: VaultCryptoScope = koinInject(),
    itemEncryptionMapper: ItemEncryptionMapper = koinInject(),
    login: Login,
    tags: List<Tag>,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit = {},
    onCopyPasswordToClipboard: (Login) -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
    ) {
        Content(
            vaultCryptoScope = vaultCryptoScope,
            itemEncryptionMapper = itemEncryptionMapper,
            login = login,
            tags = tags,
            onEditClick = onEditClick,
            onCopyPasswordToClipboard = onCopyPasswordToClipboard,
        )
    }
}

@Composable
private fun Content(
    vaultCryptoScope: VaultCryptoScope,
    itemEncryptionMapper: ItemEncryptionMapper,
    login: Login,
    tags: List<Tag>,
    onEditClick: () -> Unit = {},
    onCopyPasswordToClipboard: (Login) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var password by remember { mutableStateOf(login.password) }
    val passwordVisible by remember {
        derivedStateOf {
            when (password) {
                is SecretField.Hidden -> false
                is SecretField.Visible -> true
                null -> false
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            if (passwordVisible) {
                scope.launch(Dispatchers.IO) {
                    vaultCryptoScope.withVaultCipher(login.vaultId) {
                        password = password?.let {
                            itemEncryptionMapper.withHiddenPassword(
                                login = login,
                                vaultCipher = this,
                            )?.password
                        }
                    }
                }
            }
        }
    }

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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                LoginImage(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    iconType = login.iconType,
                    iconUrl = login.iconUrl,
                    labelText = login.labelText ?: login.defaultLabelText,
                    labelColor = login.labelColor,
                    customImageUrl = login.customImageUrl,
                    size = 50.dp,
                )

                Text(
                    text = login.name.ifEmpty { MdtLocale.strings.loginNoItemName },
                    style = MdtTheme.typo.medium.lg.copy(lineHeight = 22.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                )

                Entry(
                    title = MdtLocale.strings.loginUsername,
                    subtitle = login.username.orEmpty(),
                    isFirst = true,
                    actions = {
                        ActionsRow(useHorizontalPadding = true) {
                            IconButton(
                                icon = MdtIcons.Copy,
                                onClick = { context.copyToClipboard(login.username.orEmpty()) },
                            )
                        }
                    },
                )

                Entry(
                    title = MdtLocale.strings.loginPassword,
                    subtitleAnnotated = when (password) {
                        is SecretField.Hidden -> buildAnnotatedString { repeat(12) { append("•") } }
                        is SecretField.Visible -> passwordColorized(password = (password as SecretField.Visible).value)
                        null -> buildAnnotatedString {}
                    },
                    actions = {
                        ActionsRow(useHorizontalPadding = true) {
                            PasswordTrailingIcon(
                                passwordVisible = passwordVisible,
                                onToggle = {
                                    scope.launch(Dispatchers.IO) {
                                        vaultCryptoScope.withVaultCipher(login.vaultId) {
                                            password = when (password) {
                                                is SecretField.Hidden -> {
                                                    itemEncryptionMapper.withVisiblePassword(
                                                        login = login,
                                                        vaultCipher = this,
                                                    )?.password
                                                }

                                                is SecretField.Visible -> {
                                                    itemEncryptionMapper.withHiddenPassword(
                                                        login = login,
                                                        vaultCipher = this,
                                                    )?.password
                                                }

                                                null -> null
                                            }
                                        }
                                    }
                                },
                            )

                            IconButton(
                                icon = MdtIcons.Copy,
                                onClick = { onCopyPasswordToClipboard(login) },
                            )
                        }
                    },
                )

                login.uris.forEachIndexed { index, uri ->
                    if (uri.text.isNotEmpty()) {
                        Entry(
                            title = if (login.uris.size > 1) "URI ${index + 1}" else "URI",
                            subtitle = uri.text,
                            isCompact = true,
                            actions = {
                                ActionsRow(useHorizontalPadding = true) {
                                    IconButton(
                                        icon = MdtIcons.Open,
                                        onClick = { uriHandler.openSafely(uri.text, context) },
                                    )

                                    IconButton(
                                        icon = MdtIcons.Copy,
                                        onClick = { context.copyToClipboard(uri.text) },
                                    )
                                }
                            },
                        )
                    }
                }

                Entry(
                    title = MdtLocale.strings.loginSecurityLevel,
                    subtitle = login.securityType.asTitle(),
                    isLast = login.tagIds.isEmpty(),
                )

                if (login.tagIds.isNotEmpty()) {
                    Entry(
                        title = MdtLocale.strings.loginTags,
                        subtitle = tags.filter { login.tagIds.contains(it.id) }.joinToString(", ") { it.name },
                        isLast = true,
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
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isCompact: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MdtTheme.color.surfaceContainerHigh, RoundedShapeIndexed(isFirst, isLast))
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
                        maxLines = 3,
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

            actions()
        }
    }
}