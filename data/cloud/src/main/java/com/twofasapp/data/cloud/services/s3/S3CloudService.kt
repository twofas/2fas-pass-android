/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.s3

import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.services.common.BackupCloudService
import com.twofasapp.data.cloud.services.common.model.CloudIndexBackupJson
import java.time.Instant

internal class S3CloudService(
    s3Client: S3Client,
) : BackupCloudService<CloudConnection.S3>(s3Client) {

    override fun toFileInfo(backup: CloudIndexBackupJson): CloudFileInfo = CloudFileInfo.S3(
        deviceId = backup.deviceId,
        deviceName = backup.deviceName,
        seedHashHex = backup.seedHashHex,
        vaultId = backup.vaultId,
        vaultCreatedAt = Instant.ofEpochMilli(backup.vaultCreatedAt),
        vaultUpdatedAt = Instant.ofEpochMilli(backup.vaultUpdatedAt),
        schemaVersion = backup.schemaVersion,
    )
}