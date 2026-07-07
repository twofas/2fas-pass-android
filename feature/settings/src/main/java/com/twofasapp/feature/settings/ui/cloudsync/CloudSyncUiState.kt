/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.cloudsync

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection

internal data class CloudSyncUiState(
    val configs: List<CloudConfig> = emptyList(),
    val startGoogleAuth: Boolean = false,
) {
    val hasGoogleDrive: Boolean
        get() = configs.any { it.connection is CloudConnection.GoogleDrive }
}