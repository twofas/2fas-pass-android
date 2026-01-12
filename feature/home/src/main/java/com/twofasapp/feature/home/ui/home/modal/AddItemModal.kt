/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.modal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun AddItemModal(
    onDismissRequest: () -> Unit,
    onSelect: (ItemContentType) -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.itemAddTitle,
    ) { dismissAction ->
        Content(
            onSelect = { dismissAction { onSelect(it) } },
        )
    }
}

@Composable
private fun Content(
    onSelect: (ItemContentType) -> Unit = {},
) {
    val strings = MdtLocale.strings
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        OptionEntry(
            title = strings.contentTypeLoginName,
            icon = MdtIcons.Login,
            onClick = { onSelect(ItemContentType.Login) },
        )

        OptionEntry(
            title = strings.contentTypeSecureNoteName,
            icon = MdtIcons.SecureNote,
            onClick = { onSelect(ItemContentType.SecureNote) },
        )

        OptionEntry(
            title = strings.contentTypeCardName,
            icon = MdtIcons.PaymentCard,
            onClick = { onSelect(ItemContentType.PaymentCard) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content()
    }
}