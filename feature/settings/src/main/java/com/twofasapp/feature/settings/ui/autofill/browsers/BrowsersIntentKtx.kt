/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.autofill.browsers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Opens the browser's autofill settings page.
 * Based on: https://android-developers.googleblog.com/2025/02/chrome-3p-autofill-services-update.html
 */
internal fun Context.openBrowserAutofillSettings(packageName: String) {
    try {
        // Try to open browser-specific autofill settings
        val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_APP_BROWSER)
            addCategory(Intent.CATEGORY_PREFERENCE)
            setPackage(packageName)
        }
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        try {
            val fallbackIntent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            )
            startActivity(fallbackIntent)
        } catch (_: Exception) {
        }
    }
}