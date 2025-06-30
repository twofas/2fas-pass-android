/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.security.crypto

import com.twofasapp.core.common.crypto.normalize
import com.twofasapp.core.common.domain.crypto.KdfSpec
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.encodeHex
import timber.log.Timber

class MasterKeyGenerator(
    private val kdfCalculator: KdfCalculator,
) {
    fun generate(
        password: String,
        seedHex: String,
        saltHex: String,
        kdfSpec: KdfSpec,
    ): MasterKey {
        val inputHex = seedHex + password.normalize().toByteArray().encodeHex()

        val hashHex = kdfCalculator.kdf(
            input = inputHex.decodeHex(),
            salt = saltHex.decodeHex(),
            kdfSpec = kdfSpec,
        ).encodeHex()

        Timber.i(
            buildString {
                appendLine("[GENERATE MASTER KEY]")
                appendLine("seed+pass = $inputHex")
                appendLine("salt = $saltHex")
                appendLine("hash = $hashHex")
                appendLine("[END]")
            },
        )

        return MasterKey(
            hashHex = hashHex,
        )
    }
}