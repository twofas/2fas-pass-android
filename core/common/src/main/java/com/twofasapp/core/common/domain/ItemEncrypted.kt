/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

data class ItemEncrypted(
    override val id: String = "",
    override val vaultId: String,
    override val createdAt: Long = 0,
    override val updatedAt: Long = 0,
    override val deletedAt: Long? = null,
    override val deleted: Boolean = false,
    override val securityType: SecurityType,
    override val tagIds: List<String>,
    val contentType: String,
    val contentVersion: Int,
    val content: EncryptedBytes,
) : Item