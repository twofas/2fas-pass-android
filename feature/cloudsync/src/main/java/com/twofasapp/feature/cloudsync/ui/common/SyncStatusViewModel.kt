/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.common

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.cloud.exceptions.asCode
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.domain.CloudSyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

internal class SyncStatusViewModel(
    private val strings: Strings,
    private val cloudRepository: CloudRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(SyncStatusUiState())

    init {
        launchScoped {
            combine(
                cloudRepository.observeSyncInfo(),
                cloudRepository.observeSyncStatus(),
            ) { a, b -> Pair(a, b) }.collect { (syncInfo, syncStatus) ->

                uiState.update { state ->
                    state.copy(
                        enabled = syncInfo.enabled,
                        config = syncInfo.config,
                        status = when (syncStatus) {
                            CloudSyncStatus.Unspecified,
                            CloudSyncStatus.Synced,
                            -> {
                                if (syncInfo.enabled.not()) {
                                    "Not configured"
                                } else if (syncInfo.lastSuccessfulSyncTime == 0L) {
                                    "Not synced yet"
                                } else {
                                    "Synced ${strings.formatDuration(syncInfo.lastSuccessfulSyncTime)}"
                                }
                            }

                            CloudSyncStatus.Syncing -> "Syncing..."
                            is CloudSyncStatus.Error -> "Sync failed! (Error ${syncStatus.error.asCode()})"
                        },
                        error = syncStatus is CloudSyncStatus.Error,
                        cloudError = (syncStatus as? CloudSyncStatus.Error)?.error,
                        errorDetails = (syncStatus as? CloudSyncStatus.Error)?.error?.cause?.formatErrorDetails(),
                    )
                }
            }
        }
    }

    fun sync(forceReplace: Boolean = false) {
        launchScoped { cloudRepository.sync(forceReplace = forceReplace) }
    }

    private fun Throwable.formatErrorDetails(): String {
        return buildString {
            append("Fatal Exception: ${this@formatErrorDetails.javaClass.name}")
            append("\n")
            append(message)
            append("\n")
            append("\n")
            append(stackTrace.joinToString("\n"))
        }
    }
}