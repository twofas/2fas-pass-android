/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.twofasapp.core.android.ktx.applyAppTheme
import com.twofasapp.core.android.ktx.enableThemedEdgeToEdge
import com.twofasapp.core.android.ktx.makeWindowSecure
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.autofill.service.builders.IntentBuilders
import com.twofasapp.feature.autofill.service.builders.IntentBuilders.EXTRA_START_SCREEN
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class AutofillActivity : AppCompatActivity() {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = runBlocking { settingsRepository.observeSelectedTheme().first() }

        lifecycleScope.launch {
            settingsRepository.observeSelectedTheme().collect { theme ->
                applyAppTheme(theme)
            }
        }

        enableThemedEdgeToEdge(theme = theme)

        lifecycleScope.launch {
            settingsRepository.observeScreenCaptureEnabled().collect { allow ->
                makeWindowSecure(allow = allow)
            }
        }

        super.onCreate(savedInstanceState)

        setContent {
            window.isNavigationBarContrastEnforced = false

            AutofillContainer(
                startScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.extras!!.getSerializable(EXTRA_START_SCREEN, IntentBuilders.StartScreen::class.java) ?: IntentBuilders.StartScreen.PickLogin
                } else {
                    @Suppress("DEPRECATION")
                    intent.extras!!.getSerializable(EXTRA_START_SCREEN) as IntentBuilders.StartScreen
                },
            )
        }
    }
}