/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.passwordgenerator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.design.feature.password.PasswordGeneratorForm
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewColumn

@Composable
internal fun PasswordGeneratorModal(
    onDismissRequest: () -> Unit,
    settings: PasswordGeneratorSettings,
    onUsePasswordClick: (String, PasswordGeneratorSettings) -> Unit = { _, _ -> },
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = "Password Generator",
    ) { dismissAction ->
        Content(
            settings = settings,
            onUsePasswordClick = { pass, settings -> dismissAction { onUsePasswordClick(pass, settings) } },
        )
    }
}

@Composable
private fun Content(
    settings: PasswordGeneratorSettings,
    onUsePasswordClick: (String, PasswordGeneratorSettings) -> Unit = { _, _ -> },
) {
    var password by remember { mutableStateOf("") }
    var settings by remember { mutableStateOf(settings) }

    Column {
        PasswordGeneratorForm(
            modifier = Modifier,
            settings = settings,
            onPasswordChange = { newPassword, newSettings ->
                password = newPassword
                settings = newSettings
            },
        )

        Button(
            text = "Use password",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = { onUsePasswordClick(password, settings) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        Content(settings = PasswordGeneratorSettings())
    }
}