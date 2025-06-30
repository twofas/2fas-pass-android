/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import android.content.Context
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.serializedPref
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.data.main.domain.CloudSyncInfo
import com.twofasapp.data.main.domain.CloudSyncStatus
import com.twofasapp.data.main.local.model.CloudSyncInfoEntity
import com.twofasapp.data.main.mapper.CloudMapper
import com.twofasapp.data.main.work.CloudSyncWork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

internal class CloudRepositoryImpl(
    private val context: Context,
    private val dispatchers: Dispatchers,
    private val cloudMapper: CloudMapper,
    private val cloudServiceProvider: CloudServiceProvider,
    dataStoreOwner: DataStoreOwner,
) : CloudRepository, DataStoreOwner by dataStoreOwner {

    private val cloudSyncInfo by serializedPref(
        serializer = CloudSyncInfoEntity.serializer(),
        default = CloudSyncInfoEntity(),
        encrypted = true,
    )
    private val cloudSyncStatusFlow = MutableStateFlow<CloudSyncStatus>(CloudSyncStatus.Unspecified)

    override suspend fun enableSync(cloudConfig: CloudConfig) {
        withContext(dispatchers.io) {
            cloudSyncInfo.set(
                CloudSyncInfo(
                    enabled = true,
                    config = cloudConfig,
                    lastSuccessfulSyncTime = 0L,
                ).let(cloudMapper::mapToEntity),
            )

            sync()
        }
    }

    override suspend fun disableSync() {
        withContext(dispatchers.io) {
            val config = cloudSyncInfo.get().config
            cloudSyncInfo.delete()

            config?.let {
                cloudServiceProvider.provide(it.let(cloudMapper::mapToDomain)).disconnect()
            }
        }
    }

    override suspend fun setSyncLastTime(timestamp: Long) {
        withContext(dispatchers.io) {
            setSyncInfo(
                cloudSyncInfo.get().copy(
                    lastSuccessfulSyncTime = timestamp,
                ).let(cloudMapper::mapToDomain),
            )
        }
    }

    override suspend fun setSyncStatus(syncStatus: CloudSyncStatus) {
        cloudSyncStatusFlow.update { syncStatus }
    }

    override suspend fun setSyncInfo(syncInfo: CloudSyncInfo) {
        withContext(dispatchers.io) {
            cloudSyncInfo.set(syncInfo.let(cloudMapper::mapToEntity))
        }
    }

    override suspend fun getSyncInfo(): CloudSyncInfo {
        return withContext(dispatchers.io) {
            cloudSyncInfo.get().let(cloudMapper::mapToDomain)
        }
    }

    override suspend fun sync(forceReplace: Boolean) {
        if (cloudSyncInfo.get().enabled) {
            CloudSyncWork.dispatch(
                context = context,
                forceReplace = forceReplace,
            )
        }
    }

    override fun observeSyncInfo(): Flow<CloudSyncInfo> {
        return cloudSyncInfo.asFlow().map { it.let(cloudMapper::mapToDomain) }
    }

    override fun observeSyncStatus(): Flow<CloudSyncStatus> {
        return cloudSyncStatusFlow
    }
}