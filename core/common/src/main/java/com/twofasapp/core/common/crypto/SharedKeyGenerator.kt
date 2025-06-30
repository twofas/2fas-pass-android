/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.crypto

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyAgreement

object SharedKeyGenerator {
    fun generate(
        skEpheMa: PrivateKey,
        pkEpheBe: PublicKey,
    ): ByteArray {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(skEpheMa)
        keyAgreement.doPhase(pkEpheBe, true)
        return keyAgreement.generateSecret()
    }
}