/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.storage.migrations.data

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.serializedPref
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.main.local.CloudConfigsLocalSource
import com.twofasapp.data.main.local.model.LegacyCloudSyncInfoEntity
import com.twofasapp.data.main.local.model.LegacyCloudSyncInfoEntity.ConfigEntity
import com.twofasapp.data.main.mapper.CloudConfigMapper
import kotlinx.coroutines.withContext
import java.util.UUID

class MigrateLegacyCloudConfig(
    private val dispatchers: Dispatchers,
    private val cloudConfigsLocalSource: CloudConfigsLocalSource,
    private val cloudConfigMapper: CloudConfigMapper,
    dataStoreOwner: DataStoreOwner,
) : DataStoreOwner by dataStoreOwner {

    companion object {
        private const val Tag = "MigrateLegacyCloudConfig"
    }

    private val legacyCloudSyncInfo by serializedPref(
        serializer = LegacyCloudSyncInfoEntity.serializer(),
        default = LegacyCloudSyncInfoEntity(),
        name = "cloudSyncInfo",
        encrypted = true,
    )

    suspend fun execute() {
        withContext(dispatchers.io) {
            val legacy = legacyCloudSyncInfo.get()
            val legacyConfig = legacy.config ?: return@withContext
            if (cloudConfigsLocalSource.getAll().isNotEmpty()) return@withContext

            Flog.tag(Tag).d("Migrating legacy cloud config")

            val connection: CloudConnection = when (legacyConfig) {
                is ConfigEntity.GoogleDrive -> CloudConnection.GoogleDrive(
                    accountId = legacyConfig.id,
                    credentialType = legacyConfig.credentialType,
                )
                is ConfigEntity.WebDav -> CloudConnection.WebDav(
                    url = legacyConfig.url,
                    username = legacyConfig.username,
                    password = legacyConfig.password,
                    allowUntrustedCertificate = legacyConfig.allowUntrustedCertificate,
                )
                is ConfigEntity.S3 -> CloudConnection.S3(
                    endpoint = legacyConfig.endpoint,
                    region = legacyConfig.region,
                    bucket = legacyConfig.bucket,
                    accessKeyId = legacyConfig.accessKeyId,
                    secretAccessKey = legacyConfig.secretAccessKey,
                    allowUntrustedCertificate = legacyConfig.allowUntrustedCertificate,
                )
            }

            cloudConfigsLocalSource.save(
                cloudConfigMapper.mapToEntity(
                    domain = CloudConfig(
                        id = UUID.randomUUID().toString(),
                        syncedAt = legacy.lastSuccessfulSyncTime,
                        status = CloudSyncStatus.Idle,
                        connection = connection,
                    ),
                    createdAt = System.currentTimeMillis(),
                ),
            )

            legacyCloudSyncInfo.delete()

            Flog.tag(Tag).d("Migration completed")
        }
    }
}