/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.webdav

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.data.main.CloudRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class WebDavSyncViewModel(
    savedStateHandle: SavedStateHandle,
    private val cloudRepository: CloudRepository,
) : ViewModel() {
    private val initialConfigId: String? = savedStateHandle.toRoute<Screen.WebDavSync>().configId

    val uiState = MutableStateFlow(WebDavSyncUiState(configId = initialConfigId))

    private var observeJob: Job? = null

    init {
        initialConfigId?.let(::observeConfig)
    }

    private fun observeConfig(id: String) {
        observeJob?.cancel()
        observeJob = launchScoped {
            cloudRepository.observeConfig(id).collect { config ->
                val spec = config?.connection as? CloudConnection.WebDav
                uiState.update { state ->
                    state.copy(
                        configId = config?.id,
                        url = spec?.url ?: state.url,
                        username = spec?.username ?: state.username,
                        password = spec?.password ?: state.password,
                        allowUntrustedCertificate = spec?.allowUntrustedCertificate ?: state.allowUntrustedCertificate,
                    )
                }
            }
        }
    }

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

    fun connect() {
        launchScoped {
            uiState.update { it.copy(connecting = true, error = null) }

            val spec = CloudConnection.WebDav(
                url = uiState.value.url.trim().normalizeUrl(),
                username = uiState.value.username.trim(),
                password = uiState.value.password.trim(),
                allowUntrustedCertificate = uiState.value.allowUntrustedCertificate,
            )

            when (val result = cloudRepository.testConnection(spec)) {
                is CloudResult.Success -> {
                    val existingId = uiState.value.configId
                    if (existingId != null) {
                        cloudRepository.updateConfig(existingId, spec)
                    } else {
                        cloudRepository.addConfig(spec)
                    }
                    uiState.update { it.copy(connecting = false, closeScreen = true) }
                }

                is CloudResult.Failure -> {
                    uiState.update { it.copy(connecting = false, error = result.error.asMessage()) }
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