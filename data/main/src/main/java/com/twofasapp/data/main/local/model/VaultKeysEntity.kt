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
import androidx.room.PrimaryKey
import com.twofasapp.core.common.domain.crypto.EncryptedBytes

@Entity(
    tableName = "vault_keys",
    foreignKeys = [
        ForeignKey(
            entity = VaultEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("vault_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class VaultKeysEntity(
    @PrimaryKey
    @ColumnInfo("vault_id")
    val vaultId: String,

    @ColumnInfo("trusted")
    val trusted: EncryptedBytes?,
)