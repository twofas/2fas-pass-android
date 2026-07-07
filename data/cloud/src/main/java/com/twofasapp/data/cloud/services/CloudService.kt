/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services

import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest

interface CloudService {
    suspend fun connect(connection: CloudConnection): CloudResult
    suspend fun fetchFiles(connection: CloudConnection): List<CloudFileInfo>
    suspend fun fetchFile(connection: CloudConnection, info: CloudFileInfo): String
    suspend fun sync(
        connection: CloudConnection,
        request: VaultSyncRequest,
        mergeVaultContent: suspend (String?) -> VaultMergeResult,
    ): CloudResult

    suspend fun disconnect()
}