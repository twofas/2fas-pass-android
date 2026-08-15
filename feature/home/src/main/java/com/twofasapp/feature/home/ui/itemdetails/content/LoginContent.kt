/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.text.secretAnnotatedString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.SecretFieldType
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsEntry
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsHeader
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LoginContent(
    item: Item,
    tags: ImmutableList<Tag>,
    content: ItemContent.Login,
    decryptedFields: Map<SecretFieldType, String>,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit,
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    ItemDetailsHeader(
        item = item,
        tags = tags,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        if (content.username.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.loginUsername,
                subtitle = content.username.orEmpty(),
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("loginContentCopyUsernameButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.username.orEmpty()) },
                    )
                },
            )
        }

        content.password?.let { password ->
            ItemDetailsEntry(
                title = MdtLocale.strings.loginPassword,
                subtitleAnnotated = decryptedFields[SecretFieldType.LoginPassword]?.let {
                    passwordColorized(password = it)
                } ?: secretAnnotatedString(),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.LoginPassword] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.LoginPassword, password) },
                    )

                    IconButton(
                        modifier = Modifier.testTag("loginContentCopyPasswordButton"),
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(password) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        content.uris.forEachIndexed { index, uri ->
            if (uri.text.isNotEmpty()) {
                ItemDetailsEntry(
                    title = if (content.uris.size > 1) "${MdtLocale.strings.loginUri} ${index + 1}" else MdtLocale.strings.loginUri,
                    subtitle = uri.text,
                    isCompact = true,
                    maxLines = 3,
                    actions = {
                        IconButton(
                            modifier = Modifier.testTag("loginContentOpenUriButton${index + 1}"),
                            icon = MdtIcons.Open,
                            onClick = {
                                uriHandler.openSafely(uri.text, context)
                            },
                        )

                        IconButton(
                            modifier = Modifier.testTag("loginContentCopyUriButton${index + 1}"),
                            icon = MdtIcons.Copy,
                            onClick = { context.copyToClipboard(uri.text) },
                        )
                    },
                )
            }
        }

        if (content.notes.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.loginNotes,
                subtitle = content.notes.orEmpty(),
                isCompact = true,
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("loginContentCopyNotesButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                    )
                },
            )
        }
    }
}