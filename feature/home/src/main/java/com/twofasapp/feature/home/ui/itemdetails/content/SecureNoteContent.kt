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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.text.secretString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.SecretFieldType
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsEntry
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsHeader
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun SecureNoteContent(
    item: Item,
    tags: ImmutableList<Tag>,
    content: ItemContent.SecureNote,
    decryptedFields: Map<SecretFieldType, String>,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit,
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit,
) {
    val context = LocalContext.current

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
        content.text?.let { text ->
            ItemDetailsEntry(
                title = MdtLocale.strings.secureNoteText,
                subtitle = decryptedFields[SecretFieldType.SecureNoteText] ?: secretString(),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.SecureNoteText] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.SecureNoteText, text) },
                    )

                    IconButton(
                        modifier = Modifier.testTag("secureNoteContentCopyTextButton"),
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(text) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        if (content.additionalInfo.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.secureNoteAdditionalInfoLabel,
                subtitle = content.additionalInfo.orEmpty(),
                isCompact = true,
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("secureNoteContentCopyAdditionalInfoButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.additionalInfo.orEmpty()) },
                    )
                },
            )
        }
    }
}