/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.resetApp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.feature.developer.ui.DeveloperUiState

@Composable
internal fun ItemsSection(
    uiState: DeveloperUiState,
    onGenerateItems: (SecurityType) -> Unit = {},
    onGenerateMultipleItems: (Int) -> Unit = {},
    onGenerateTopDomainItems: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
    onDeleteAllBrowsers: () -> Unit = {},
    onInsertRandomTag: () -> Unit = {},
    onInsertRandomSecureNote: () -> Unit = {},
    onInsertRandomCreditCard: () -> Unit = {},
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Items in Vault: ${uiState.loginItemsCount}",
            style = MdtTheme.typo.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MdtTheme.color.tertiary,
            modifier = Modifier
                .padding(top = 24.dp, bottom = 12.dp, start = 18.dp)
                .fillMaxWidth(),
        )

        OptionEntry(
            title = "Generate Tier 1 item",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateItems(SecurityType.Tier1) },
        )

        OptionEntry(
            title = "Generate Tier 2 item",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateItems(SecurityType.Tier2) },
        )

        OptionEntry(
            title = "Generate Tier 3 item",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateItems(SecurityType.Tier3) },
        )

        OptionEntry(
            title = "Generate 100 items",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateMultipleItems(100) },
        )

        OptionEntry(
            title = "Generate 1000 items",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateMultipleItems(1000) },
        )

        OptionEntry(
            title = "Generate 10000 items",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateMultipleItems(10000) },
        )

        OptionEntry(
            title = "Generate 1000 domains with favicon",
            icon = MdtIcons.Refresh,
            onClick = { onGenerateTopDomainItems() },
        )

        OptionEntry(
            title = "Generate secure note",
            icon = MdtIcons.SecureNote,
            onClick = { onInsertRandomSecureNote() },
        )

        OptionEntry(
            title = "Generate credit card",
            icon = MdtIcons.PaymentCard,
            onClick = { onInsertRandomCreditCard() },
        )

        OptionEntry(
            title = "Insert random tag",
            icon = MdtIcons.Tag,
            onClick = { onInsertRandomTag() },
        )

        OptionEntry(
            title = "Delete all items",
            icon = MdtIcons.DeleteForever,
            iconTint = MdtTheme.color.error,
            titleColor = MdtTheme.color.error,
            onClick = { onDeleteAll() },
        )

        OptionEntry(
            title = "Delete all extensions",
            icon = MdtIcons.DeleteForever,
            iconTint = MdtTheme.color.error,
            titleColor = MdtTheme.color.error,
            onClick = { onDeleteAllBrowsers() },
        )

        OptionEntry(
            title = "Factory reset",
            icon = MdtIcons.Restore,
            iconTint = MdtTheme.color.error,
            titleColor = MdtTheme.color.error,
            onClick = { context.resetApp() },
        )
    }
}