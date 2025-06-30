/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.cloudfiles

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.feature.startup.ui.restorevault.RestoreFile
import com.twofasapp.feature.startup.ui.restorevault.RestoreState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class CloudFilesViewModel(
    private val cloudServiceProvider: CloudServiceProvider,
    private val restoreState: RestoreState,
) : ViewModel() {

    val uiState = MutableStateFlow(
        CloudFilesUiState(),
    )

    init {
        launchScoped(Dispatchers.IO) {
            restoreState.cloudConfig?.let { config ->
                runSafely { cloudServiceProvider.provide(config).fetchFiles(config) }
                    .onSuccess { files ->
                        uiState.update {
                            it.copy(
                                loading = false,
                                files = files.sortedByDescending { f -> f.vaultUpdatedAt },
                            )
                        }
                    }
                    .onFailure {
                        uiState.update {
                            it.copy(
                                loading = false,
                                files = emptyList(),
                            )
                        }
                    }
            }
        }
    }

    fun selectFile(file: CloudFileInfo) {
        restoreState.restoreFile = RestoreFile.Cloud(
            fileInfo = file,
        )
    }
}