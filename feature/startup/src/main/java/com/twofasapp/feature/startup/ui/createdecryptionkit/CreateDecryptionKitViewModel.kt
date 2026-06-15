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
import com.twofasapp.feature.startup.ui.StartupProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CreateDecryptionKitViewModel(
    private val startupProcessor: StartupProcessor,
) : ViewModel() {
    val uiState = MutableStateFlow(CreateDecryptionKitUiState())

    init {
        launchScoped {
            val seed = startupProcessor.getSeed()
            val masterKey = startupProcessor.getMasterKey()

            uiState.update {
                it.copy(
                    decryptionKit = DecryptionKit(
                        words = seed.words,
                        entropy = seed.entropyHex.decodeHex(),
                        masterKey = masterKey.hashHex.decodeHex(),
                    ),
                )
            }
        }
    }
}