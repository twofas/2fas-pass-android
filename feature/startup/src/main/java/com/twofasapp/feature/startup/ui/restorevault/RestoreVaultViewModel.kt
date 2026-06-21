/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.feature.startup.ui.StartupProcessor
import kotlinx.coroutines.flow.MutableStateFlow

internal class RestoreVaultViewModel(
    private val startupProcessor: StartupProcessor,
    private val restoreState: RestoreState,
) : ViewModel() {

    val uiState = MutableStateFlow(RestoreVaultUiState())

    init {
        launchScoped {
            startupProcessor.clearStartupData()
        }
    }

    fun updateRestoreCloudConfig(connection: CloudConnection) {
        restoreState.cloudConnection = connection
    }

    fun updateRestoreSource(source: RestoreSource) {
        restoreState.restoreSource = source
    }

    fun backupFilePicked(uri: Uri) {
        restoreState.restoreFile = RestoreFile.LocalFile(uri = uri)
    }
}