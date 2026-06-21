/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.s3

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.feature.startup.ui.restorevault.RestoreState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class S3RestoreViewModel(
    private val cloudServiceProvider: CloudServiceProvider,
    private val restoreState: RestoreState,
) : ViewModel() {

    val uiState = MutableStateFlow(
        S3RestoreUiState(),
    )

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

    fun connect(
        onConnectSuccess: () -> Unit,
        onConnectFailure: (String) -> Unit,
    ) {
        uiState.update { it.copy(loading = true) }

        launchScoped {
            val connection = CloudConnection.S3(
                endpoint = uiState.value.endpoint.trim().normalizeUrl(),
                region = uiState.value.region.trim(),
                bucket = uiState.value.bucket.trim(),
                accessKeyId = uiState.value.accessKeyId.trim(),
                secretAccessKey = uiState.value.secretAccessKey.trim(),
                allowUntrustedCertificate = uiState.value.allowUntrustedCertificate,
            )

            when (val result = cloudServiceProvider.provide(connection).connect(connection)) {
                is CloudResult.Success -> {
                    uiState.update { it.copy(loading = false) }
                    restoreState.cloudConnection = connection
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