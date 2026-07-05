/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import android.content.Context
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.cloud.services.CloudServiceProvider
import com.twofasapp.data.main.local.CloudConfigsLocalSource
import com.twofasapp.data.main.mapper.CloudConfigMapper
import com.twofasapp.data.main.work.CloudSyncWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

internal class CloudRepositoryImpl(
    private val context: Context,
    private val dispatchers: Dispatchers,
    private val cloudConfigMapper: CloudConfigMapper,
    private val cloudServiceProvider: CloudServiceProvider,
    private val cloudConfigsLocalSource: CloudConfigsLocalSource,
    dataStoreOwner: DataStoreOwner,
) : CloudRepository, DataStoreOwner by dataStoreOwner {

    init {
        CoroutineScope(dispatchers.io + SupervisorJob()).launch {
            runSafely { clearStaleSyncingStatuses() }
                .onFailure { Flog.persist(tag = "CloudSync", message = "clearStaleSyncingStatuses failed: ${it.message}") }
        }
    }

    override suspend fun testConnection(connection: CloudConnection): CloudResult {
        return withContext(dispatchers.io) {
            cloudServiceProvider.provide(connection).connect(connection)
        }
    }

    override suspend fun addConfig(connection: CloudConnection): String {
        return withContext(dispatchers.io) {
            val id = UUID.randomUUID().toString()
            cloudConfigsLocalSource.save(
                cloudConfigMapper.mapToEntity(
                    domain = CloudConfig(
                        id = id,
                        syncedAt = 0L,
                        status = CloudSyncStatus.Idle,
                        connection = connection,
                    ),
                    createdAt = System.currentTimeMillis(),
                ),
            )
            sync()
            id
        }
    }

    override suspend fun updateConfig(id: String, connection: CloudConnection) {
        withContext(dispatchers.io) {
            val existing = cloudConfigsLocalSource.get(id) ?: return@withContext
            cloudConfigsLocalSource.save(
                cloudConfigMapper.mapToEntity(
                    domain = CloudConfig(
                        id = id,
                        syncedAt = existing.syncedAt,
                        status = CloudSyncStatus.Idle,
                        connection = connection,
                    ),
                    createdAt = existing.createdAt,
                ),
            )
            sync()
        }
    }

    override suspend fun removeConfig(id: String) {
        withContext(dispatchers.io) {
            val entity = cloudConfigsLocalSource.get(id) ?: return@withContext
            cloudConfigsLocalSource.delete(id)
            val config = cloudConfigMapper.mapToDomain(entity)
            runSafely { cloudServiceProvider.provide(config.connection).disconnect() }
                .onFailure { Flog.persist(tag = "CloudSync", message = "disconnect() failed for $id: ${it.message}") }
        }
    }

    override suspend fun getConfig(id: String): CloudConfig? {
        return withContext(dispatchers.io) {
            cloudConfigsLocalSource.get(id)?.let { cloudConfigMapper.mapToDomain(it) }
        }
    }

    override suspend fun getConfigs(): List<CloudConfig> {
        return withContext(dispatchers.io) {
            cloudConfigsLocalSource.getAll().map { cloudConfigMapper.mapToDomain(it) }
        }
    }

    override suspend fun reencryptConfigs(configs: List<CloudConfig>) {
        withContext(dispatchers.io) {
            configs.forEach { config ->
                val existing = cloudConfigsLocalSource.get(config.id) ?: return@forEach
                cloudConfigsLocalSource.save(cloudConfigMapper.mapToEntity(domain = config, createdAt = existing.createdAt))
            }
        }
    }

    override suspend fun setSyncStatus(id: String, status: CloudSyncStatus) {
        withContext(dispatchers.io) {
            cloudConfigsLocalSource.updateStatus(
                id = id,
                status = cloudConfigMapper.statusName(status),
                errorCode = cloudConfigMapper.errorCode(status),
            )
        }
    }

    override suspend fun setSyncLastTime(id: String, timestamp: Long) {
        withContext(dispatchers.io) {
            cloudConfigsLocalSource.updateLastSyncTime(id, timestamp)
        }
    }

    override suspend fun sync(forceReplace: Boolean) {
        if (cloudConfigsLocalSource.getAll().isNotEmpty()) {
            CloudSyncWork.dispatch(
                context = context,
                forceReplace = forceReplace,
            )
        }
    }

    override fun observeConfigs(): Flow<List<CloudConfig>> {
        return cloudConfigsLocalSource.observeAll().map { entities ->
            entities.map { cloudConfigMapper.mapToDomain(it) }
        }
    }

    override fun observeConfig(id: String): Flow<CloudConfig?> {
        return cloudConfigsLocalSource.observe(id).map { entity ->
            entity?.let { cloudConfigMapper.mapToDomain(it) }
        }
    }

    override fun observeAggregateStatus(): Flow<CloudSyncStatus> {
        return observeConfigs().map { configs ->
            val statuses = configs.map { it.status }
            when {
                statuses.any { it is CloudSyncStatus.Syncing } -> CloudSyncStatus.Syncing
                statuses.any { it is CloudSyncStatus.Error } ->
                    statuses.first { it is CloudSyncStatus.Error }
                statuses.any { it is CloudSyncStatus.Synced } -> CloudSyncStatus.Synced
                else -> CloudSyncStatus.Idle
            }
        }
    }

    private suspend fun clearStaleSyncingStatuses() {
        cloudConfigsLocalSource.getAll()
            .filter { it.status == CloudConfigMapper.StatusSyncing }
            .forEach { entity ->
                cloudConfigsLocalSource.updateStatus(
                    id = entity.id,
                    status = CloudConfigMapper.StatusIdle,
                    errorCode = null,
                )
            }
    }
}