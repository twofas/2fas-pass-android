/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.window

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.twofasapp.core.android.ktx.currentActivity

@Composable
fun ScreenOrientation(compactOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
    if (LocalInspectionMode.current.not()) {
        val activity = LocalContext.currentActivity
        val deviceType = currentDeviceType()
        val isTablet = activity.resources.configuration.smallestScreenWidthDp >= 600

        when {
            isTablet.not() || deviceType == DeviceType.Compact -> {
                DisposableEffect(Unit) {
                    val originalOrientation = activity.requestedOrientation
                    activity.requestedOrientation = compactOrientation
                    onDispose {
                        activity.requestedOrientation = originalOrientation
                    }
                }
            }

            deviceType == DeviceType.Medium -> Unit
            deviceType == DeviceType.Expanded -> Unit
        }
    }
}