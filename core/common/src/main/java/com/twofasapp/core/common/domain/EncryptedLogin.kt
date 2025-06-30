/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

data class EncryptedLogin(
    val id: String = "",
    val vaultId: String,
    val name: EncryptedBytes,
    val username: EncryptedBytes?,
    val password: EncryptedBytes?,
    val securityType: LoginSecurityType,
    val uris: List<EncryptedLoginUri>,
    val iconType: IconType,
    val iconUriIndex: Int? = null,
    val customImageUrl: EncryptedBytes? = null,
    val labelText: EncryptedBytes? = null,
    val labelColor: String? = null,
    val notes: EncryptedBytes? = null,
    val tags: List<String>,
    val deleted: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long? = null,
)