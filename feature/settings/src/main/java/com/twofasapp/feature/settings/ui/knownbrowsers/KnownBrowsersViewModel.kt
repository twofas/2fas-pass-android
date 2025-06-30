/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.knownbrowsers

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.state.empty
import com.twofasapp.core.design.state.success
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.domain.ConnectedBrowser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class KnownBrowsersViewModel(
    private val strings: Strings,
    private val connectedBrowsersRepository: ConnectedBrowsersRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(KnownBrowsersUiState())
    val screenState = MutableStateFlow(ScreenState.Loading)

    init {
        launchScoped {
            connectedBrowsersRepository.observeBrowsers().collect { browsers ->
                uiState.update { it.copy(connectedBrowsers = browsers.sortedByDescending { b -> b.createdAt }) }

                if (browsers.isEmpty()) {
                    screenState.empty(strings.knownBrowsersEmpty)
                } else {
                    screenState.success()
                }
            }
        }
    }

    fun delete(browser: ConnectedBrowser) {
        launchScoped {
            connectedBrowsersRepository.deleteBrowser(browser)
        }
    }
}