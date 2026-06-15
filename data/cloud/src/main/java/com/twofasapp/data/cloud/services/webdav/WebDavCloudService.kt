/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.webdav

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.services.common.BackupCloudService
import com.twofasapp.data.cloud.services.common.model.CloudIndexBackupJson
import java.time.Instant

internal class WebDavCloudService(
    webDavClient: WebDavClient,
) : BackupCloudService<CloudConfig.WebDav>(webDavClient) {

    override fun toFileInfo(backup: CloudIndexBackupJson): CloudFileInfo = CloudFileInfo.WebDav(
        deviceId = backup.deviceId,
        deviceName = backup.deviceName,
        seedHashHex = backup.seedHashHex,
        vaultId = backup.vaultId,
        vaultCreatedAt = Instant.ofEpochMilli(backup.vaultCreatedAt),
        vaultUpdatedAt = Instant.ofEpochMilli(backup.vaultUpdatedAt),
        schemaVersion = backup.schemaVersion,
    )
}