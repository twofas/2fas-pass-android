/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.common

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.exceptions.CloudError

internal data class SyncStatusUiState(
    val enabled: Boolean = false,
    val config: CloudConfig? = null,
    val status: String = "",
    val error: Boolean = false,
    val cloudError: CloudError? = null,
    val errorDetails: String? = null,
)