/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.security.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lambdapioneer.argon2kt.Argon2Version
import com.twofasapp.core.common.domain.crypto.KdfSpec

internal class KdfCalculatorImpl : KdfCalculator {
    private val argon2Kt = Argon2Kt()

    override fun kdf(
        input: ByteArray,
        salt: ByteArray,
        kdfSpec: KdfSpec,
    ): ByteArray {
        return when (kdfSpec) {
            is KdfSpec.Argon2id -> kdfArgon2(input = input, salt = salt, spec = kdfSpec)
        }
    }

    private fun kdfArgon2(
        input: ByteArray,
        salt: ByteArray,
        spec: KdfSpec.Argon2id,
    ): ByteArray {
        return argon2Kt
            .hash(
                mode = Argon2Mode.ARGON2_ID,
                password = input,
                salt = salt,
                tCostInIterations = spec.iterations,
                mCostInKibibyte = spec.memoryMb * 1024,
                parallelism = spec.parallelism,
                hashLengthInBytes = spec.hashLength,
                version = Argon2Version.V13,
            ).rawHashAsByteArray()
    }
}