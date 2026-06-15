/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.s3

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

internal class S3SyncViewModel(
    private val cloudRepository: CloudRepository,
    private val vaultsRepository: VaultsRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    val uiState = MutableStateFlow(S3SyncUiState())

    init {
        launchScoped {
            combine(
                cloudRepository.observeSyncInfo(),
                cloudRepository.observeSyncStatus(),
            ) { a, b -> Pair(a, b) }.collect { (syncInfo, syncStatus) ->
                uiState.update { state ->
                    val config = syncInfo.config as? CloudConfig.S3
                    state.copy(
                        syncEnabled = syncInfo.enabled,
                        syncing = syncStatus == CloudSyncStatus.Syncing,
                        endpoint = config?.endpoint ?: state.endpoint,
                        region = config?.region ?: state.region,
                        bucket = config?.bucket ?: state.bucket,
                        accessKeyId = config?.accessKeyId ?: state.accessKeyId,
                        secretAccessKey = config?.secretAccessKey ?: state.secretAccessKey,
                        allowUntrustedCertificate = config?.allowUntrustedCertificate
                            ?: state.allowUntrustedCertificate,
                    )
                }
            }
        }
    }

    fun updateEndpoint(endpoint: String) {
        uiState.update { it.copy(endpoint = endpoint) }
    }

    fun updateRegion(region: String) {
        uiState.update { it.copy(region = region) }
    }

    fun updateBucket(bucket: String) {
        uiState.update { it.copy(bucket = bucket) }
    }

    fun updateAccessKeyId(accessKeyId: String) {
        uiState.update { it.copy(accessKeyId = accessKeyId) }
    }

    fun updateSecretAccessKey(secretAccessKey: String) {
        uiState.update { it.copy(secretAccessKey = secretAccessKey) }
    }

    fun toggleAllowUntrustedCertificate() {
        uiState.update { it.copy(allowUntrustedCertificate = it.allowUntrustedCertificate.not()) }
    }

    fun connect() {
        launchScoped {
            cloudRepository.enableSync(
                CloudConfig.S3(
                    endpoint = uiState.value.endpoint.trim().normalizeUrl(),
                    region = uiState.value.region.trim(),
                    bucket = uiState.value.bucket.trim(),
                    accessKeyId = uiState.value.accessKeyId.trim(),
                    secretAccessKey = uiState.value.secretAccessKey.trim(),
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