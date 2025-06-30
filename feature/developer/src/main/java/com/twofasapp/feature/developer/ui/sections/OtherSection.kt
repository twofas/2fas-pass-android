/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.developer.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.feature.developer.ui.DeveloperUiState

@Composable
internal fun OtherSection(
    uiState: DeveloperUiState,
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Build",
            style = MdtTheme.typo.labelLargeProminent,
            color = MdtTheme.color.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        ItemRow(
            label = "Variant: ",
            value = when (uiState.appBuild?.buildVariant) {
                BuildVariant.Release -> "release"
                BuildVariant.Internal -> "internal"
                BuildVariant.Debug -> "debug"
                else -> "unknown"
            },
        )

        ItemRow(
            label = "Version name: ",
            value = uiState.appBuild?.versionName.toString(),
        )

        ItemRow(
            label = "Version code: ",
            value = uiState.appBuild?.versionCode.toString(),
        )

        Text(
            text = "Security",
            style = MdtTheme.typo.labelLargeProminent,
            color = MdtTheme.color.primary,
            modifier = Modifier.padding(bottom = 8.dp, top = 20.dp),
        )

        ItemColumn(
            label = "Seed Hex:",
            value = uiState.seed?.seedHex.toString(),
        )

        ItemColumn(
            label = "Salt Hex:",
            value = uiState.seed?.saltHex.toString(),
        )

        ItemColumn(
            label = "Entropy Hex:",
            value = uiState.seed?.entropyHex.toString(),
        )

        ItemColumn(
            label = "Words:",
            value = uiState.seed?.words.toString(),
        )
    }
}

@Composable
internal fun ItemRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() ?: context.copyToClipboard(value) },
    ) {
        Text(
            text = label,
            style = MdtTheme.typo.labelLarge,
            color = MdtTheme.color.onSurface,
        )

        Text(
            text = value,
            style = MdtTheme.typo.labelLargeProminent,
            color = MdtTheme.color.primary,
        )
    }
}

@Composable
internal fun ItemColumn(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() ?: context.copyToClipboard(value) }
            .padding(bottom = 4.dp),
    ) {
        Text(
            text = label,
            style = MdtTheme.typo.labelLarge,
            color = MdtTheme.color.onSurface,
        )

        Text(
            text = value,
            style = MdtTheme.typo.labelLargeProminent,
            color = MdtTheme.color.primary,
        )
    }
}