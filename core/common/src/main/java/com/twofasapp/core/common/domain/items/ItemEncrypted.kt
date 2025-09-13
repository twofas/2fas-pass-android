/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.crypto.EncryptedBytes

data class ItemEncrypted(
    override val id: String,
    override val vaultId: String,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val deletedAt: Long?,
    override val deleted: Boolean,
    override val securityType: SecurityType,
    override val tagIds: List<String>,
    override val contentType: String,
    override val contentVersion: Int,
    val content: EncryptedBytes,
) : ItemSpec