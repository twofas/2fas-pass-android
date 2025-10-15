/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.toastShort
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.feature.developer.ui.sections.ItemsSection
import com.twofasapp.feature.developer.ui.sections.OtherSection
import com.twofasapp.feature.developer.ui.sections.SubscriptionSection
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun DeveloperScreen(
    viewModel: DeveloperViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Content(
        uiState = uiState,
        onGenerateItems = { viewModel.generateTestItems(it) },
        onGenerateMultipleItems = {
            viewModel.generateRandomTestItems(it) {
                context.toastShort("Done!")
            }
        },
        onGenerateTopDomainItems = {
            viewModel.generateTopDomainItems {
                context.toastShort("Done!")
            }
        },
        onSetSubscriptionOverride = { viewModel.setSubscriptionOverride(it) },
        onDeleteAll = { viewModel.deleteAll() },
        onInsertRandomTag = { viewModel.insertRandomTag() },
        onInsertRandomSecureNote = { viewModel.insertRandomSecureNote() },
    )
}

@Composable
private fun Content(
    uiState: DeveloperUiState,
    onGenerateItems: (SecurityType) -> Unit = {},
    onGenerateMultipleItems: (Int) -> Unit = {},
    onGenerateTopDomainItems: () -> Unit = {},
    onSetSubscriptionOverride: (String?) -> Unit = {},
    onDeleteAll: () -> Unit = {},
    onInsertRandomTag: () -> Unit = {},
    onInsertRandomSecureNote: () -> Unit = {},
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Items", "Subscription", "Other")

    Scaffold(
        topBar = { TopAppBar(title = "Developer") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(padding),
        ) {
            TabRow(
                modifier = Modifier.background(MdtTheme.color.background),
                selectedTabIndex = selectedTabIndex,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        modifier = Modifier.background(MdtTheme.color.background),
                        text = {
                            Text(
                                text = title,
                                style = MdtTheme.typo.titleSmall,
                                color = if (selectedTabIndex == index) MdtTheme.color.primary else MdtTheme.color.onSurfaceVariant,
                            )
                        },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> ItemsSection(
                    uiState = uiState,
                    onGenerateItems = onGenerateItems,
                    onGenerateMultipleItems = onGenerateMultipleItems,
                    onGenerateTopDomainItems = onGenerateTopDomainItems,
                    onDeleteAll = onDeleteAll,
                    onInsertRandomTag = onInsertRandomTag,
                    onInsertRandomSecureNote = onInsertRandomSecureNote,
                )

                1 -> SubscriptionSection(
                    uiState = uiState,
                    onSetSubscriptionOverride = onSetSubscriptionOverride,
                )

                2 -> OtherSection(
                    uiState = uiState,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = DeveloperUiState(),
        )
    }
}