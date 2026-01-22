/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.autofill

import android.content.Intent
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.settings.ui.autofill.browsers.openBrowserAutofillSettings
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AutofillScreen(
    viewModel: AutofillViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onAutofillInlineToggle = { viewModel.updateAutofillInline() },
        onCheckBrowsersStatus = { viewModel.checkBrowsersStatus() },
    )
}

@Composable
private fun Content(
    uiState: AutofillUiState,
    onAutofillInlineToggle: () -> Unit = {},
    onCheckBrowsersStatus: () -> Unit = {},
) {
    val activity = LocalContext.currentActivity
    val context = LocalContext.current
    val strings = MdtLocale.strings
    val autofillManager: AutofillManager = activity.getSystemService(AutofillManager::class.java)
    var autofillServiceEnabled: Boolean by remember { mutableStateOf(autofillManager.hasEnabledAutofillServices()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        autofillServiceEnabled = autofillManager.hasEnabledAutofillServices()
        onCheckBrowsersStatus()
    }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntryAutofill) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionHeader(
                text = strings.settingsAutofillSystem,
                contentPadding = OptionHeaderContentPaddingFirst,
            )

            OptionSwitch(
                title = strings.settingsAutofillService,
                subtitle = strings.settingsAutofillServiceDesc,
                icon = MdtIcons.Autofill,
                checked = autofillServiceEnabled,
                onToggle = {
                    if (autofillServiceEnabled) {
                        autofillManager.disableAutofillServices()
                        autofillServiceEnabled = false
                    } else {
                        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
                        intent.setData("package:${uiState.packageName}".toUri())
                        activity.startActivity(intent)
                    }
                },
            )

            OptionSwitch(
                title = strings.settingsAutofillKeyboard,
                subtitle = strings.settingsAutofillKeyboardDesc,
                icon = MdtIcons.AutofillInline,
                checked = uiState.autofillInline,
                enabled = autofillServiceEnabled,
                onToggle = { onAutofillInlineToggle() },
            )

            if (autofillServiceEnabled && uiState.browsers.isNotEmpty()) {
                OptionHeader(
                    text = strings.settingsAutofillBrowsers,
                )

                Text(
                    text = strings.settingsAutofillBrowsersDescription,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                )

                uiState.browsers.forEach { browser ->
                    OptionSwitch(
                        title = browser.name,
                        image = browser.icon ?: MdtIcons.Autofill,
                        checked = browser.autofillEnabled,
                        onToggle = { context.openBrowserAutofillSettings(browser.packageName) },
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = 8.dp,
                        ),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = AutofillUiState(),
        )
    }
}