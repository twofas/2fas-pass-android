/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.data.cloud.exceptions.CloudError

sealed interface CloudSyncStatus {
    data object Unspecified : CloudSyncStatus
    data object Syncing : CloudSyncStatus
    data object Synced : CloudSyncStatus
    data class Error(val error: CloudError) : CloudSyncStatus
}