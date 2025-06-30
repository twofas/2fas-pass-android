/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeHex

data class VaultHashes(
    val vaultId: String,
    val trusted: ByteArray,
    val secret: ByteArray,
    val external: ByteArray,
) {
    val seedHashBase64: String
        get() = external.encodeBase64()

    val seedHashHex: String
        get() = external.encodeHex()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultHashes

        if (vaultId != other.vaultId) return false
        if (!trusted.contentEquals(other.trusted)) return false
        if (!secret.contentEquals(other.secret)) return false
        if (!external.contentEquals(other.external)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vaultId.hashCode()
        result = 31 * result + trusted.contentHashCode()
        result = 31 * result + secret.contentHashCode()
        result = 31 * result + external.contentHashCode()
        return result
    }
}