/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.cloudfiles

import com.twofasapp.data.cloud.domain.CloudFileInfo

internal data class CloudFilesUiState(
    val loading: Boolean = true,
    val files: List<CloudFileInfo> = emptyList(),
)