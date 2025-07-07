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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = VaultEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("vault_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["vault_id"])],
)
data class ItemEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("vault_id")
    val vaultId: String,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("updated_at")
    val updatedAt: Long,
    @ColumnInfo("deleted_at")
    val deletedAt: Long?,
    @ColumnInfo("deleted")
    val deleted: Boolean,
    @ColumnInfo("security_type")
    val securityType: Int,
    @ColumnInfo("content_type")
    val contentType: String,
    @ColumnInfo("content_version")
    val contentVersion: String,
    @ColumnInfo("content")
    val content: String,
    @ColumnInfo("tag_ids")
    val tagIds: List<String>?,
)