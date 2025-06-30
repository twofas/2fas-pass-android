/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connect

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.ktx.decodeBase64ToString
import com.twofasapp.data.main.BrowserExtensionRepository
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.settings.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

internal class ConnectViewModel(
    private val appBuild: AppBuild,
    private val sessionRepository: SessionRepository,
    private val browserExtensionRepository: BrowserExtensionRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(
        ConnectUiState(
            debuggable = appBuild.debuggable,
        ),
    )
    private var scannerEnabled: Boolean = true

    init {
        launchScoped {
            sessionRepository.observeConnectOnboardingPrompted().collect { prompted ->
                val connectOnboardingPrompted = when (appBuild.buildVariant) {
                    BuildVariant.Release -> prompted
                    BuildVariant.Internal -> prompted
                    BuildVariant.Debug -> true
                }

                uiState.update { it.copy(connectOnboardingPrompted = connectOnboardingPrompted) }
            }
        }
    }

    fun enableScanner(enabled: Boolean) {
        scannerEnabled = enabled

        uiState.update { it.copy(scannerEnabled = enabled) }
    }

    fun setOnboardingPrompted(prompted: Boolean) {
        launchScoped {
            sessionRepository.setConnectOnboardingPrompted(prompted)
        }
    }

    fun scanned(text: String, onCompleted: () -> Unit) {
        if (scannerEnabled.not()) return

        enableScanner(false)

        Timber.d("Scanned: $text")

        launchScoped {
            runSafely {
                with(text.decodeBase64ToString().split(":")) {
                    ConnectData(
                        version = this[0],
                        sessionId = this[1],
                        pkPersBeHex = this[2],
                        pkEpheBeHex = this[3],
                        signatureHex = this[4],
                    )
                }
            }
                .onSuccess { connectData ->
                    Timber.d("Scanned data: $connectData")

                    browserExtensionRepository.publishConnect(connectData)
                    onCompleted()
                }
                .onFailure { e -> enableScanner(true) }
        }
    }
}