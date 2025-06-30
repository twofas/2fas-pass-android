/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createdecryptionkit

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.feature.decryptionkit.generator.DecryptionKit
import com.twofasapp.feature.startup.ui.StartupConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CreateDecryptionKitViewModel(
    private val startupConfig: StartupConfig,
) : ViewModel() {
    val uiState = MutableStateFlow(CreateDecryptionKitUiState())

    init {
        launchScoped {
            uiState.update {
                it.copy(
                    decryptionKit = DecryptionKit(
                        words = startupConfig.seed!!.words,
                        entropy = startupConfig.seed!!.entropyHex.decodeHex(),
                        masterKey = startupConfig.masterKey!!.hashHex.decodeHex(),
                    ),
                )
            }
        }
    }
}