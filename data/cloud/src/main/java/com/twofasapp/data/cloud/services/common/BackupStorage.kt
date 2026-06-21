/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.common

import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.services.common.model.CloudIndexJson

internal interface BackupStorage<C : CloudConnection> {
    suspend fun testConnection(config: C) = Unit
    suspend fun getIndex(config: C): CloudIndexJson
    suspend fun putIndex(config: C, index: CloudIndexJson)
    suspend fun getFile(config: C, filename: String): String?
    suspend fun putFile(config: C, filename: String, content: String)
    suspend fun moveFile(config: C, source: String, destination: String)
    suspend fun obtainLock(config: C): Boolean
    suspend fun releaseLock(config: C)
}