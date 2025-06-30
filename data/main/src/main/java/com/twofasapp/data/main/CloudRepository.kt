/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.main.domain.CloudSyncInfo
import com.twofasapp.data.main.domain.CloudSyncStatus
import kotlinx.coroutines.flow.Flow

interface CloudRepository {
    suspend fun enableSync(cloudConfig: CloudConfig)
    suspend fun disableSync()
    suspend fun setSyncStatus(syncStatus: CloudSyncStatus)
    suspend fun setSyncInfo(syncInfo: CloudSyncInfo)
    suspend fun setSyncLastTime(timestamp: Long)
    suspend fun getSyncInfo(): CloudSyncInfo
    suspend fun sync(forceReplace: Boolean = false)
    fun observeSyncInfo(): Flow<CloudSyncInfo>
    fun observeSyncStatus(): Flow<CloudSyncStatus>
}