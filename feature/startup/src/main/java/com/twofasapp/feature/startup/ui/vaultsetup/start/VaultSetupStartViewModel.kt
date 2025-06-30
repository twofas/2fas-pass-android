/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.vaultsetup.start

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.feature.startup.ui.StartupConfig

internal class VaultSetupStartViewModel(
    private val startupConfig: StartupConfig,
) : ViewModel() {

    fun clearStartupData() {
        launchScoped {
            startupConfig.clear()
            startupConfig.clearStorage()
        }
    }
}