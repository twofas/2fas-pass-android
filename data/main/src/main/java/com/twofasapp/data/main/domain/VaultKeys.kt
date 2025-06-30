/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

data class VaultKeys(
    val vaultId: String,
    val trusted: EncryptedBytes?,
    val secret: EncryptedBytes?,
    val external: EncryptedBytes?,
) {
    val valid: Boolean
        get() = trusted != null && secret != null && external != null
}