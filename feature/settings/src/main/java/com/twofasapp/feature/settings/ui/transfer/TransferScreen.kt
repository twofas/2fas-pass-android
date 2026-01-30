/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.R
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.purchases.PurchasesDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun TransferScreen(
    viewModel: TransferViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onExternalImportClick = { type ->
            deeplinks.openScreen(Screen.ExternalImport(type))
        },
    )
}

@Composable
private fun Content(
    uiState: TransferUiState,
    onExternalImportClick: (ImportType) -> Unit = {},
) {
    val strings = MdtLocale.strings
    var showPaywall by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntryTransferFromOtherApps) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionEntry(
                title = null,
                subtitle = strings.transferServicesDisclaimer,
                contentPadding = PaddingValues(horizontal = 16.dp),
            )

            OptionHeader(
                text = strings.settingsEntryImportOtherApps,
            )

            ImportType.entries.sortedBy { it.displayName }.forEach { type ->
                OptionEntry(
                    title = type.displayName,
                    image = painterResource(
                        id = when (type) {
                            ImportType.Bitwarden -> R.drawable.external_logo_bitwarden
                            ImportType.OnePassword -> R.drawable.external_logo_onepassword
                            ImportType.ProtonPass -> R.drawable.external_logo_protonpass
                            ImportType.Chrome -> R.drawable.external_logo_chrome
                            ImportType.MicrosoftEdge -> R.drawable.external_logo_microsoft_edge
                            ImportType.Enpass -> R.drawable.external_logo_enpass
                            ImportType.LastPass -> R.drawable.external_logo_lastpass
                            ImportType.DashlaneDesktop -> R.drawable.external_logo_dashlane
                            ImportType.DashlaneMobile -> R.drawable.external_logo_dashlane
                            ImportType.AppleDesktop -> R.drawable.external_logo_apple
                            ImportType.AppleMobile -> R.drawable.external_logo_apple
                            ImportType.Firefox -> R.drawable.external_logo_firefox
                            ImportType.KeePass -> R.drawable.external_logo_keepass
                            ImportType.KeePassXC -> R.drawable.external_logo_keepassxc
                            ImportType.Keeper -> R.drawable.external_logo_keeper
                            ImportType.NordPass -> R.drawable.external_logo_nordpass
                        },
                    ),
                    onClick = {
                        if (uiState.isItemsLimitReached) {
                            showPaywall = true
                        } else {
                            onExternalImportClick(type)
                        }
                    },
                )
            }
        }
    }

    if (showPaywall) {
        PurchasesDialog(
            title = MdtLocale.strings.paywallNoticeItemsLimitTransferTitle,
            body = MdtLocale.strings.paywallNoticeItemsLimitTransferMsg.format(uiState.maxItems),
            onDismissRequest = { showPaywall = false },
        )
    }
}

@Preview
@Composable
private fun PreviewDark() {
    PreviewTheme(appTheme = AppTheme.Dark) {
        Content(
            uiState = TransferUiState(),
        )
    }
}

@Preview
@Composable
private fun PreviewLight() {
    PreviewTheme(appTheme = AppTheme.Light) {
        Content(
            uiState = TransferUiState(),
        )
    }
}