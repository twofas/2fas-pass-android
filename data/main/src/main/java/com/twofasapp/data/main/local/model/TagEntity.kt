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

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("vault_id")
    val vaultId: String,
    @ColumnInfo("updated_at")
    val updatedAt: Long,
    @ColumnInfo("name")
    val name: EncryptedBytes,
    @ColumnInfo("color")
    val color: String?,
    @ColumnInfo("position")
    val position: Int,
)