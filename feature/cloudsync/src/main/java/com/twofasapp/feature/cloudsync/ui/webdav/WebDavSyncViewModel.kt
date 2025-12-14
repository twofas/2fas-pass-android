/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.webdav

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.CloudSyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

internal class WebDavSyncViewModel(
    private val cloudRepository: CloudRepository,
    private val vaultsRepository: VaultsRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    val uiState = MutableStateFlow(
        WebDavSyncUiState(),
    )

    init {
        launchScoped {
            combine(
                cloudRepository.observeSyncInfo(),
                cloudRepository.observeSyncStatus(),
            ) { a, b -> Pair(a, b) }.collect { (syncInfo, syncStatus) ->
                uiState.update { state ->
                    state.copy(
                        syncEnabled = syncInfo.enabled,
                        syncing = syncStatus == CloudSyncStatus.Syncing,
                        url = syncInfo.config?.let { (it as CloudConfig.WebDav).url } ?: state.url,
                        username = syncInfo.config?.let { (it as CloudConfig.WebDav).username } ?: state.username,
                        password = syncInfo.config?.let { (it as CloudConfig.WebDav).password } ?: state.password,
                        allowUntrustedCertificate = syncInfo.config?.let { (it as CloudConfig.WebDav).allowUntrustedCertificate }
                            ?: state.allowUntrustedCertificate,
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
            cloudRepository.enableSync(
                CloudConfig.WebDav(
                    url = uiState.value.url.trim().normalizeUrl(),
                    username = uiState.value.username.trim(),
                    password = uiState.value.password.trim(),
                    allowUntrustedCertificate = uiState.value.allowUntrustedCertificate,
                ),
            )
        }
    }

    fun disconnect() {
        launchScoped { cloudRepository.disableSync() }
    }

    fun sync() {
        launchScoped {
            cloudRepository.sync(forceReplace = false)

            vaultsRepository.setUpdatedTimestamp(
                id = vaultsRepository.getVault().id,
                timestamp = timeProvider.currentTimeUtc(),
            )
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