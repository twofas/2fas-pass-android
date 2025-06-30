/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services

import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudFileInfo
import com.twofasapp.data.cloud.domain.CloudResult
import com.twofasapp.data.cloud.domain.VaultMergeResult
import com.twofasapp.data.cloud.domain.VaultSyncRequest

interface CloudService {
    suspend fun connect(config: CloudConfig): CloudResult
    suspend fun fetchFiles(config: CloudConfig): List<CloudFileInfo>
    suspend fun fetchFile(config: CloudConfig, info: CloudFileInfo): String
    suspend fun sync(
        config: CloudConfig,
        request: VaultSyncRequest,
        mergeVaultContent: suspend (String?) -> VaultMergeResult,
    ): CloudResult

    suspend fun disconnect()
}