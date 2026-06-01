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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonHeight
import com.twofasapp.core.network.ApiConfig
import com.twofasapp.core.network.ApiEnvironment
import com.twofasapp.feature.developer.ui.DeveloperUiState

@Composable
internal fun EnvironmentSection(
    uiState: DeveloperUiState,
    onSelectEnvironment: (ApiEnvironment) -> Unit = {},
    onSaveAndRestart: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Environment",
            style = MdtTheme.typo.labelLargeProminent,
            color = MdtTheme.color.primary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier.selectableGroup(),
        ) {
            OptionEntry(
                title = "Production",
                subtitle = buildString {
                    appendLine(ApiConfig.ProductionApiUrl)
                    appendLine(ApiConfig.ProductionShareApiUrl)
                    appendLine(ApiConfig.ProductionWssUrl)
                },
                onClick = { onSelectEnvironment(ApiEnvironment.Production) },
                content = {
                    RadioButton(
                        selected = uiState.selectedEnvironment == ApiEnvironment.Production,
                        onClick = null,
                    )
                },
            )

            OptionEntry(
                title = "Development",
                subtitle = buildString {
                    appendLine(ApiConfig.DevApiUrl)
                    appendLine(ApiConfig.DevShareApiUrl)
                    appendLine(ApiConfig.DevWssUrl)
                },
                onClick = { onSelectEnvironment(ApiEnvironment.Dev) },
                content = {
                    RadioButton(
                        selected = uiState.selectedEnvironment == ApiEnvironment.Dev,
                        onClick = null,
                    )
                },
            )
        }

        Button(
            text = "Save and restart",
            size = ButtonHeight.Small,
            onClick = onSaveAndRestart,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        )
    }
}