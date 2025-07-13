/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.securitytier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.richText
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SecurityTierScreen(
    viewModel: SecurityTierViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onChange = viewModel::onChange,
    )
}

@Composable
private fun Content(
    uiState: SecurityTierUiState,
    onChange: (SecurityType) -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntrySecurityTier) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionHeader(
                text = strings.settingsHeaderSecurityTier,
                contentPadding = OptionHeaderContentPaddingFirst,
            )
            listOf(
                SecurityType.Tier3,
                SecurityType.Tier2,
                SecurityType.Tier1,
            ).forEach { type ->
                OptionEntry(
                    title = when (type) {
                        SecurityType.Tier1 -> strings.settingsEntrySecurityTier1
                        SecurityType.Tier2 -> strings.settingsEntrySecurityTier2
                        SecurityType.Tier3 -> strings.settingsEntrySecurityTier3
                    },
                    subtitleAnnotated = richText(
                        when (type) {
                            SecurityType.Tier1 -> strings.settingsEntrySecurityTier1Desc
                            SecurityType.Tier2 -> strings.settingsEntrySecurityTier2Desc
                            SecurityType.Tier3 -> strings.settingsEntrySecurityTier3Desc
                        },
                    ),
                    icon = when (type) {
                        SecurityType.Tier3 -> MdtIcons.Tier3
                        SecurityType.Tier2 -> MdtIcons.Tier2
                        SecurityType.Tier1 -> MdtIcons.Tier1
                    },
                    onClick = { onChange(type) },
                    content = {
                        RadioButton(
                            selected = uiState.defaultSecurityLevel == type,
                            onClick = { onChange(type) },
                        )
                    },
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
            uiState = SecurityTierUiState(),
        )
    }
}