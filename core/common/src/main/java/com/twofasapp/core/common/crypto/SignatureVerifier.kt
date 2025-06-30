/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.crypto

import java.security.Signature

object SignatureVerifier {
    fun verify(
        key: ByteArray,
        data: ByteArray,
        signature: ByteArray,
    ): Boolean {
        val publicKey = EcKeyConverter.createPublicKey(key)
        val signatureDer = convertRawToDer(signature)

        val signatureInstance = Signature.getInstance("SHA256withECDSA")
        signatureInstance.initVerify(publicKey)
        signatureInstance.update(data)
        return signatureInstance.verify(signatureDer)
    }

    private fun convertRawToDer(rawSignature: ByteArray): ByteArray {
        // Extract r and s components from the 64-byte raw signature (32 bytes each)
        val r = rawSignature.copyOfRange(0, 32)
        val s = rawSignature.copyOfRange(32, 64)

        // Prepend 0x00 to r or s if it starts with a byte > 0x7F (negative in two's complement)
        val rAdjusted = if (r[0] < 0) byteArrayOf(0) + r else r
        val sAdjusted = if (s[0] < 0) byteArrayOf(0) + s else s

        // Calculate lengths for DER encoding
        val rLength = rAdjusted.size
        val sLength = sAdjusted.size
        val totalLength = 2 + rLength + 2 + sLength // Sequence header (2) + r part (2 + len) + s part (2 + len)

        // Build DER structure using ByteArray
        return byteArrayOf(
            0x30, // Sequence tag
            totalLength.toByte(), // Total length of the sequence
        ) + byteArrayOf(
            0x02, // Integer tag for r
            rLength.toByte(), // Length of r
        ) + rAdjusted + byteArrayOf(
            0x02, // Integer tag for s
            sLength.toByte(), // Length of s
        ) + sAdjusted
    }
}