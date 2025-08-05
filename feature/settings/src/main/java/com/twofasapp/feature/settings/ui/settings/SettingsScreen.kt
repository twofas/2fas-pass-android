/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import com.twofasapp.feature.purchases.PurchasesDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        deeplinks = deeplinks,
    )
}

@Composable
private fun Content(
    uiState: SettingsUiState,
    deeplinks: Deeplinks,
) {
    val strings = MdtLocale.strings
    var showPaywall by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val lazyListState: LazyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                content = { Text(text = strings.settingsTitle, style = MdtTheme.typo.medium.xl2) },
                showBackButton = false,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
            state = lazyListState,
            contentPadding = PaddingValues(bottom = ScreenPadding),
        ) {
            // Preferences section
            listItem(SettingsListItem.Header(strings.settingsHeaderPrefs)) {
                OptionHeader(
                    text = strings.settingsHeaderPrefs,
                    contentPadding = OptionHeaderContentPaddingFirst,
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntrySecurity)) {
                OptionEntry(
                    title = strings.settingsEntrySecurity,
                    subtitle = strings.settingsEntrySecurityDesc,
                    icon = MdtIcons.Encrypted,
                    onClick = { deeplinks.openScreen(Screen.Security) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryCustomization)) {
                OptionEntry(
                    title = strings.settingsEntryCustomization,
                    subtitle = strings.settingsEntryCustomizationDesc,
                    icon = MdtIcons.Brush,
                    onClick = { deeplinks.openScreen(Screen.Customization) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryAutofill)) {
                OptionEntry(
                    title = strings.settingsEntryAutofill,
                    subtitle = strings.settingsEntryAutofillDesc,
                    icon = MdtIcons.AutofillInput,
                    onClick = { deeplinks.openScreen(Screen.Autofill) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryTrash)) {
                OptionEntry(
                    title = strings.settingsEntryTrash,
                    subtitle = strings.settingsEntryTrashDesc,
                    icon = MdtIcons.Delete,
                    onClick = { deeplinks.openScreen(Screen.Trash) },
                )
            }

            // Browser Extension section
            listItem(SettingsListItem.Header(strings.settingsHeaderBrowserExtension)) {
                OptionHeader(
                    text = strings.settingsHeaderBrowserExtension,
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryKnownBrowsers)) {
                OptionEntry(
                    title = strings.settingsEntryKnownBrowsers,
                    subtitle = strings.settingsEntryKnownBrowsersDesc,
                    icon = MdtIcons.Desktop,
                    onClick = { deeplinks.openScreen(Screen.KnownBrowsers) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryPushNotification)) {
                OptionEntry(
                    title = strings.settingsEntryPushNotification,
                    subtitle = strings.settingsEntryPushNotificationDesc,
                    icon = MdtIcons.Notifications,
                    onClick = { deeplinks.openScreen(Screen.PushNotifications) },
                )
            }

            // Backup section
            listItem(SettingsListItem.Header(strings.settingsHeaderBackup)) {
                OptionHeader(
                    text = strings.settingsHeaderBackup,
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryCloudSync)) {
                OptionEntry(
                    title = strings.settingsEntryCloudSync,
                    subtitle = strings.settingsEntryCloudSyncDesc,
                    icon = MdtIcons.CloudSync,
                    onClick = {
                        when (uiState.cloudConfig) {
                            is CloudConfig.GoogleDrive -> deeplinks.openScreen(Screen.GoogleDriveSync(openedFromQuickSetup = false, startAuth = false))
                            is CloudConfig.WebDav -> deeplinks.openScreen(Screen.WebDavSync)
                            null -> deeplinks.openScreen(Screen.CloudSync)
                        }
                    },
                    content = {
                        if (uiState.cloudSyncError) {
                            Icon(
                                painter = MdtIcons.Error,
                                contentDescription = null,
                                tint = MdtTheme.color.error,
                            )
                        }
                    },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryImportExport)) {
                OptionEntry(
                    title = strings.settingsEntryImportExport,
                    subtitle = strings.settingsEntryImportExportDesc,
                    icon = MdtIcons.ImportExport,
                    onClick = { deeplinks.openScreen(Screen.ImportExport) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryTransferFromOtherApps)) {
                OptionEntry(
                    title = strings.settingsEntryTransferFromOtherApps,
                    subtitle = strings.settingsEntryTransferFromOtherAppsDesc,
                    icon = MdtIcons.SyncAlt,
                    onClick = { deeplinks.openScreen(Screen.TransferFromOtherApps) },
                )
            }

            // About section
            listItem(SettingsListItem.Header(strings.settingsHeaderAbout)) {
                OptionHeader(
                    text = strings.settingsHeaderAbout,
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntrySubscription)) {
                when (uiState.subscriptionPlan) {
                    is SubscriptionPlan.Free -> {
                        OptionEntry(
                            title = strings.settingsEntrySubscription,
                            subtitle = "Upgrade your plan for more features.",
                            value = uiState.subscriptionPlan.displayName,
                            icon = MdtIcons.Star,
                            onClick = { showPaywall = true },
                        )
                    }

                    is SubscriptionPlan.Paid -> {
                        OptionEntry(
                            title = strings.settingsEntrySubscription,
                            subtitle = "Check your current plan.",
                            value = uiState.subscriptionPlan.displayName,
                            icon = MdtIcons.Star,
                            onClick = { deeplinks.openScreen(Screen.ManageSubscription) },
                        )
                    }
                }
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryAbout)) {
                OptionEntry(
                    title = strings.settingsEntryAbout,
                    icon = MdtIcons.Info,
                    onClick = { deeplinks.openScreen(Screen.About) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryHelpCenter)) {
                OptionEntry(
                    title = strings.settingsEntryHelpCenter,
                    icon = MdtIcons.Support,
                    external = true,
                    onClick = { uriHandler.openSafely(MdtLocale.links.support) },
                )
            }

            listItem(SettingsListItem.Entry(strings.settingsEntryDiscord)) {
                OptionEntry(
                    title = strings.settingsEntryDiscord,
                    icon = MdtIcons.Forum,
                    external = true,
                    onClick = { uriHandler.openSafely(MdtLocale.links.discord) },
                )
            }
        }
    }

    if (showPaywall) {
        PurchasesDialog(
            onDismissRequest = { showPaywall = false },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = SettingsUiState(cloudSyncError = true),
            deeplinks = Deeplinks.Empty,
        )
    }
}