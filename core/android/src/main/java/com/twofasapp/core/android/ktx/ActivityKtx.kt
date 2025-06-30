/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.ktx

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.twofasapp.core.common.domain.SelectedTheme

fun Context.applyAppTheme(theme: SelectedTheme) {
    getSystemService(UiModeManager::class.java)
        .setApplicationNightMode(
            when (theme) {
                SelectedTheme.Light -> UiModeManager.MODE_NIGHT_NO
                SelectedTheme.Dark -> UiModeManager.MODE_NIGHT_YES
                SelectedTheme.Auto -> UiModeManager.MODE_NIGHT_AUTO
            },
        )
}

fun ComponentActivity.enableThemedEdgeToEdge(theme: SelectedTheme) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            lightScrim = Color.Transparent.toArgb(),
            darkScrim = Color.Transparent.toArgb(),
            detectDarkMode = {
                when (theme) {
                    SelectedTheme.Auto -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    SelectedTheme.Light -> false
                    SelectedTheme.Dark -> true
                }
            },
        ),
        navigationBarStyle = SystemBarStyle.auto(
            lightScrim = Color.Transparent.toArgb(),
            darkScrim = Color.Transparent.toArgb(),
            detectDarkMode = {
                when (theme) {
                    SelectedTheme.Auto -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    SelectedTheme.Light -> false
                    SelectedTheme.Dark -> true
                }
            },
        ),
    )
}