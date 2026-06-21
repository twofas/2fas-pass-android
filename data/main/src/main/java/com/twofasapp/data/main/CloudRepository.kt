/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import kotlinx.coroutines.flow.Flow

interface CloudRepository {
    suspend fun testConnection(connection: CloudConnection): CloudResult
    suspend fun addConfig(connection: CloudConnection): String
    suspend fun updateConfig(id: String, connection: CloudConnection)
    suspend fun removeConfig(id: String)
    suspend fun getConfig(id: String): CloudConfig?
    suspend fun getConfigs(): List<CloudConfig>
    suspend fun setSyncStatus(id: String, status: CloudSyncStatus)
    suspend fun setSyncLastTime(id: String, timestamp: Long)
    suspend fun sync(forceReplace: Boolean = false)
    fun observeConfigs(): Flow<List<CloudConfig>>
    fun observeConfig(id: String): Flow<CloudConfig?>
    fun observeAggregateStatus(): Flow<CloudSyncStatus>
}