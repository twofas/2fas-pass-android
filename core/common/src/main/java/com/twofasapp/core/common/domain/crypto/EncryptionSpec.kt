/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain.crypto

data class EncryptionSpec(
    val seedHash: String,
    val reference: String,
    val kdfSpec: KdfSpec,
) {
    companion object {
        val Empty = EncryptionSpec("", "", KdfSpec.Argon2id())
    }
}