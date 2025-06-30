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
import com.twofasapp.data.cloud.domain.CloudConfig
import kotlinx.coroutines.flow.MutableStateFlow

internal class RestoreVaultViewModel(
    private val restoreState: RestoreState,
) : ViewModel() {

    val uiState = MutableStateFlow(RestoreVaultUiState())

    fun updateRestoreCloudConfig(config: CloudConfig) {
        restoreState.cloudConfig = config
    }

    fun updateRestoreSource(source: RestoreSource) {
        restoreState.restoreSource = source
    }

    fun backupFilePicked(uri: Uri) {
        restoreState.restoreFile = RestoreFile.LocalFile(uri = uri)
    }
}