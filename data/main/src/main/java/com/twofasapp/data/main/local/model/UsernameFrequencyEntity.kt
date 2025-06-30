/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.model

import androidx.room.ColumnInfo
import com.twofasapp.core.common.domain.crypto.EncryptedBytes

data class UsernameFrequencyEntity(
    @ColumnInfo("vault_id")
    val vaultId: String,
    @ColumnInfo(name = "username")
    val username: EncryptedBytes?,
    @ColumnInfo(name = "security_type")
    val securityType: Int,
    @ColumnInfo(name = "occurrences")
    val occurrences: Int,
)