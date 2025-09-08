/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connectmodal

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.ConnectWebSocketResult
import com.twofasapp.data.main.domain.IdenticonGenerator
import com.twofasapp.data.main.websocket.ConnectWebSocket
import com.twofasapp.data.purchases.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ConnectModalViewModel(
    private val strings: Strings,
    private val purchasesRepository: PurchasesRepository,
    private val connectedBrowsersRepository: ConnectedBrowsersRepository,
    private val connectWebSocket: ConnectWebSocket,
) : ViewModel() {

    val uiState = MutableStateFlow(ConnectModalUiState())

    private var isNewBrowser: Boolean = false

    fun connect(connectData: ConnectData, confirmed: Boolean = false) {
        uiState.update {
            it.copy(
                browserExtensionName = null,
                browserIdenticon = IdenticonGenerator.generate(connectData.pkPersBe),
                connectData = connectData,
                connectState = ConnectState.Loading,
            )
        }

        launchScoped {
            val connectedBrowser = connectedBrowsersRepository.getBrowser(connectData.pkPersBe)

            if (connectData.version > ConnectData.CurrentSchema + 1) {
                // Always support 1 version behind
                updateState(ConnectState.AppUpdateRequired)
                return@launchScoped
            }

            if (connectData.version + 1 < ConnectData.CurrentSchema) {
                // Always support 1 version behind
                updateState(ConnectState.BrowserExtensionUpdateRequired)
                return@launchScoped
            }

            if (connectedBrowser == null && confirmed.not()) {
                isNewBrowser = true

                if (isEligibleForMultipleBrowsers()) {
                    updateState(ConnectState.ConfirmNewExtension)
                } else {
                    updateState(ConnectState.UpgradePlan)
                }

                return@launchScoped
            }

            uiState.update { it.copy(browserExtensionName = connectedBrowser?.extensionName) }

            updateState(ConnectState.Loading)

            val result = connectWebSocket.open(
                connectData = connectData,
            )

            when (result) {
                is ConnectWebSocketResult.Success -> {
                    if (isNewBrowser) {
                        val savedBrowser = connectedBrowsersRepository.getBrowser(connectData.pkPersBe)

                        uiState.update {
                            it.copy(
                                browserExtensionName = savedBrowser?.extensionName,
                                browserIdenticon = savedBrowser?.identicon,
                            )
                        }

                        updateState(ConnectState.Success)
                    } else {
                        uiState.update { it.copy(finishWithSuccess = true) }
                    }
                }

                is ConnectWebSocketResult.Failure -> {
                    updateState(
                        ConnectState.Error(
                            title = strings.connectModalErrorGenericTitle,
                            subtitle = result.errorMessage.ifEmpty { strings.connectModalErrorGenericSubtitle },
                            cta = strings.connectModalErrorGenericCta,
                        ),
                    )
                }
            }
        }
    }

    private fun updateState(state: ConnectState) {
        uiState.update { it.copy(connectState = state) }
    }

    private suspend fun isEligibleForMultipleBrowsers(): Boolean {
        return connectedBrowsersRepository.hasAnyBrowsers().not() ||
            purchasesRepository.getSubscriptionPlan().entitlements.unlimitedConnectedBrowsers
    }
}