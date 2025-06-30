/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.webdav

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.feature.startup.ui.restorevault.RestoreState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class WebDavRestoreViewModel(
    private val cloudServiceProvider: CloudServiceProvider,
    private val restoreState: RestoreState,
) : ViewModel() {

    val uiState = MutableStateFlow(
        WebDavRestoreUiState(),
    )

    fun updateUrl(url: String) {
        uiState.update { it.copy(url = url) }
    }

    fun updateUsername(username: String) {
        uiState.update { it.copy(username = username) }
    }

    fun updatePassword(password: String) {
        uiState.update { it.copy(password = password) }
    }

    fun toggleAllowUntrustedCertificate() {
        uiState.update { it.copy(allowUntrustedCertificate = it.allowUntrustedCertificate.not()) }
    }

    fun connect(
        onConnectSuccess: () -> Unit,
        onConnectFailure: (String) -> Unit,
    ) {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            val config = CloudConfig.WebDav(
                url = uiState.value.url.trim().normalizeUrl(),
                username = uiState.value.username.trim(),
                password = uiState.value.password.trim(),
                allowUntrustedCertificate = uiState.value.allowUntrustedCertificate,
            )

            when (val result = cloudServiceProvider.provide(config).connect(config)) {
                is CloudResult.Success -> {
                    uiState.update { it.copy(loading = false) }
                    restoreState.cloudConfig = config
                    onConnectSuccess()
                }

                is CloudResult.Failure -> {
                    uiState.update { it.copy(loading = false) }

                    onConnectFailure(result.error.asMessage())
                }
            }
        }
    }

    private fun String.normalizeUrl(): String =
        this.trim()
            .let {
                if (it.startsWith("http://", true).not() && it.startsWith("https://", true).not()) {
                    "https://$it"
                } else {
                    it
                }
            }
            .removeSuffix("/")
}