/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.savedecryptionkit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.data.main.SecurityRepository
import com.twofasapp.feature.decryptionkit.generator.DecryptionKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SaveDecryptionKitViewModel(
    savedStateHandle: SavedStateHandle,
    private val securityRepository: SecurityRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(SaveDecryptionKitUiState())

    init {
        launchScoped {
            val seed = securityRepository.getSeed()

            uiState.update {
                it.copy(
                    decryptionKit = DecryptionKit(
                        words = seed.words,
                        entropy = seed.entropyHex.decodeHex(),
                        masterKey = savedStateHandle.toRoute<Screen.SaveDecryptionKit>().masterKeyHex.decodeHex(),
                    ),
                )
            }
        }
    }
}