/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.twofasapp.core.common.domain.crypto.EncryptedBytes

@Entity(tableName = "connected_browsers")
data class ConnectedBrowserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int,
    @ColumnInfo("public_key")
    val publicKey: EncryptedBytes,
    @ColumnInfo("extension_name")
    val extensionName: EncryptedBytes,
    @ColumnInfo("browser_name")
    val browserName: EncryptedBytes,
    @ColumnInfo("browser_version")
    val browserVersion: EncryptedBytes,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("last_sync_at")
    val lastSyncAt: Long,
    @ColumnInfo("next_session_id")
    val nextSessionId: EncryptedBytes,
)