/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.resetApp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.feature.autofill.service.builders.AutofillActivityIntents
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import com.twofasapp.feature.developer.ui.DeveloperUiState
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
internal fun ItemsSection(
    uiState: DeveloperUiState,
    onGenerateItems: (SecurityType) -> Unit = {},
    onGenerateMultipleItems: (Int) -> Unit = {},
    onGenerateTopDomainItems: () -> Unit = {},
    onDeleteAllItems: () -> Unit = {},
    onDeleteAllTags: () -> Unit = {},
    onDeleteAllBrowsers: () -> Unit = {},
    onInsertRandomTag: () -> Unit = {},
    onInsertRandomSecureNote: () -> Unit = {},
    onInsertRandomCreditCard: () -> Unit = {},
    onInsertRandomWifi: () -> Unit = {},
) {
    val context = LocalContext.current
    val autofillActivityIntents = koinInject<AutofillActivityIntents>()
    var showGenerateItemsMenu by remember { mutableStateOf(false) }

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

        Box(
            contentAlignment = Alignment.TopEnd,
        ) {
            OptionEntry(
                title = "Generate Items",
                icon = MdtIcons.Refresh,
                onClick = { showGenerateItemsMenu = true },
            )

            DropdownMenu(
                expanded = showGenerateItemsMenu,
                onDismissRequest = { showGenerateItemsMenu = false },
                offset = DpOffset(x = (-16).dp, y = 0.dp),
            ) {
                DropdownMenuItem(
                    text = { Text("100 items") },
                    onClick = {
                        showGenerateItemsMenu = false
                        onGenerateMultipleItems(100)
                    },
                )
                DropdownMenuItem(
                    text = { Text("1000 items") },
                    onClick = {
                        showGenerateItemsMenu = false
                        onGenerateMultipleItems(1000)
                    },
                )
                DropdownMenuItem(
                    text = { Text("10000 items") },
                    onClick = {
                        showGenerateItemsMenu = false
                        onGenerateMultipleItems(10000)
                    },
                )
                DropdownMenuItem(
                    text = { Text("1000 domains with favicon") },
                    onClick = {
                        showGenerateItemsMenu = false
                        onGenerateTopDomainItems()
                    },
                )
            }
        }

        OptionEntry(
            title = "Generate Login - Tier 1",
            icon = MdtIcons.Login,
            onClick = { onGenerateItems(SecurityType.Tier1) },
        )

        OptionEntry(
            title = "Generate Login - Tier 2",
            icon = MdtIcons.Login,
            onClick = { onGenerateItems(SecurityType.Tier2) },
        )

        OptionEntry(
            title = "Generate Login - Tier 3",
            icon = MdtIcons.Login,
            onClick = { onGenerateItems(SecurityType.Tier3) },
        )

        OptionEntry(
            title = "Generate Secure Note",
            icon = MdtIcons.SecureNote,
            onClick = { onInsertRandomSecureNote() },
        )

        OptionEntry(
            title = "Generate Credit Card",
            icon = MdtIcons.PaymentCard,
            onClick = { onInsertRandomCreditCard() },
        )

        OptionEntry(
            title = "Generate WiFi",
            icon = MdtIcons.Wifi4Bar,
            onClick = { onInsertRandomWifi() },
        )

        OptionEntry(
            title = "Insert random tag",
            icon = MdtIcons.Tag,
            onClick = { onInsertRandomTag() },
        )

        OptionEntry(
            title = "Open Autofill Save",
            icon = MdtIcons.AutofillInput,
            onClick = {
                val random = Random.nextInt().toString().take(3)

                context.startActivity(
                    autofillActivityIntents.createSaveLoginIntent(
                        context = context,
                        saveLoginData = SaveLoginData(
                            uri = "id$random.autofill.com",
                            username = "user$random@mail.com",
                            password = "pass$random",
                        ),
                    ),
                )
            },
        )

        OptionEntry(
            title = "Open Autofill Picker",
            icon = MdtIcons.Autofill,
            onClick = {
                val random = Random.nextInt().toString().take(3)

                autofillActivityIntents.createPickerPendingIntent(
                    context = context,
                    nodeStructure = NodeStructure(
                        packageName = "com.app",
                        webDomain = "id$random.autofill.com",
                        inputs = emptyList(),
                    ),
                    inlinePresentationSpec = null,
                ).send()
            },
        )

        OptionEntry(
            title = "Delete all tags",
            icon = MdtIcons.DeleteForever,
            iconTint = MdtTheme.color.error,
            titleColor = MdtTheme.color.error,
            onClick = { onDeleteAllTags() },
        )

        OptionEntry(
            title = "Delete all items",
            icon = MdtIcons.DeleteForever,
            iconTint = MdtTheme.color.error,
            titleColor = MdtTheme.color.error,
            onClick = { onDeleteAllItems() },
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