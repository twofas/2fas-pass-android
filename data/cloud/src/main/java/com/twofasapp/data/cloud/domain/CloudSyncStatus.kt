/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.domain

import com.twofasapp.data.cloud.exceptions.CloudError

sealed interface CloudSyncStatus {
    data object Idle : CloudSyncStatus
    data object Syncing : CloudSyncStatus
    data object Synced : CloudSyncStatus
    data class Error(val error: CloudError) : CloudSyncStatus
}