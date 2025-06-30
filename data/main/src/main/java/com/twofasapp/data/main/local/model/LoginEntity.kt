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
import com.twofasapp.core.common.domain.crypto.EncryptedBytes

@Entity(
    tableName = "logins",
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
data class LoginEntity(
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
    @ColumnInfo("name")
    val name: EncryptedBytes,
    @ColumnInfo("username")
    val username: EncryptedBytes?,
    @ColumnInfo("password")
    val password: EncryptedBytes?,
    @ColumnInfo("security_type")
    val securityType: Int,
    @ColumnInfo("uris")
    val uris: List<String>?,
    @ColumnInfo("icon_type")
    val iconType: Int,
    @ColumnInfo("icon_uri_index")
    val iconUriIndex: Int?,
    @ColumnInfo("custom_image_url")
    val customImageUrl: EncryptedBytes?,
    @ColumnInfo("label_text")
    val labelText: EncryptedBytes?,
    @ColumnInfo("label_color")
    val labelColor: String?,
    @ColumnInfo("notes")
    val notes: EncryptedBytes?,
    @ColumnInfo("tags")
    val tags: List<String>?,
)