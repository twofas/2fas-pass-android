/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.s3

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.VaultsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class S3SyncViewModel(
    savedStateHandle: SavedStateHandle,
    private val cloudRepository: CloudRepository,
    private val vaultsRepository: VaultsRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val initialConfigId: String? = savedStateHandle.toRoute<Screen.S3Sync>().configId

    val uiState = MutableStateFlow(S3SyncUiState(configId = initialConfigId))

    private var observeJob: Job? = null

    init {
        initialConfigId?.let(::observeConfig)
    }

    private fun observeConfig(id: String) {
        observeJob?.cancel()
        observeJob = launchScoped {
            cloudRepository.observeConfig(id).collect { config ->
                val spec = config?.connection as? CloudConnection.S3
                uiState.update { state ->
                    state.copy(
                        configId = config?.id,
                        endpoint = spec?.endpoint ?: state.endpoint,
                        region = spec?.region ?: state.region,
                        bucket = spec?.bucket ?: state.bucket,
                        accessKeyId = spec?.accessKeyId ?: state.accessKeyId,
                        secretAccessKey = spec?.secretAccessKey ?: state.secretAccessKey,
                        allowUntrustedCertificate = spec?.allowUntrustedCertificate
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
            uiState.update { it.copy(connecting = true, error = null) }

            val spec = CloudConnection.S3(
                endpoint = uiState.value.endpoint.trim().normalizeUrl(),
                region = uiState.value.region.trim(),
                bucket = uiState.value.bucket.trim(),
                accessKeyId = uiState.value.accessKeyId.trim(),
                secretAccessKey = uiState.value.secretAccessKey.trim(),
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