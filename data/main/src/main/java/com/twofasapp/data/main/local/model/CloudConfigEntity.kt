/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloud_configs")
data class CloudConfigEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("synced_at")
    val syncedAt: Long,
    @ColumnInfo("status")
    val status: String,
    @ColumnInfo("error_code")
    val errorCode: String?,
    @ColumnInfo("connection")
    val connection: String,
)