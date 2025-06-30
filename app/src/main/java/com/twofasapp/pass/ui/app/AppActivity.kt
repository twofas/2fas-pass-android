/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.ui.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.ktx.applyAppTheme
import com.twofasapp.core.android.ktx.enableThemedEdgeToEdge
import com.twofasapp.core.android.ktx.makeWindowSecure
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.pass.work.DisableScreenCaptureWork
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class AppActivity : AppCompatActivity() {

    private val settingsRepository: SettingsRepository by inject()
    private val deeplinks: Deeplinks by inject()

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

                if (allow) {
                    DisableScreenCaptureWork.dispatch(this@AppActivity)
                }
            }
        }

        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContent {
            window.isNavigationBarContrastEnforced = false

            AppContainer()
        }

        lifecycleScope.launch {
            deeplinks.onCreate(this@AppActivity, intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            deeplinks.onNewIntent(this@AppActivity, intent)
        }
    }
}