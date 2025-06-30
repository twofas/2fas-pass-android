/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HkdfGenerator {

    fun generate(
        inputKeyMaterial: ByteArray, // Input Keying Material (e.g., ECDH shared secret)
        salt: ByteArray, // Salt
        contextInfo: String, // Contextual info for key binding
        outputKeyLength: Int = 32, // Desired output key length in bytes
    ): ByteArray {
        // Input validation
        require(inputKeyMaterial.isNotEmpty()) { "Input key material cannot be empty" }
        require(salt.isNotEmpty()) { "Salt cannot be empty" }
        require(outputKeyLength > 0) { "Output length must be positive" }
        require(outputKeyLength <= 255 * 32) { "Output length exceeds HKDF limit (255 * HashLen)" }

        try {
            val hmacSha256 = Mac.getInstance("HmacSHA256")
            val hashLen = 32 // SHA-256 output length

            // Step 1: Extract - Generate PRK
            hmacSha256.init(SecretKeySpec(salt, "HmacSHA256"))
            val pseudoRandomKey = hmacSha256.doFinal(inputKeyMaterial)

            // Step 2: Expand - Derive output key
            val result = ByteArray(outputKeyLength)
            var bytesGenerated = 0
            var blockNumber = 1
            val contextBytes = contextInfo.toByteArray(Charsets.UTF_8)

            hmacSha256.init(SecretKeySpec(pseudoRandomKey, "HmacSHA256"))

            var previousBlock = ByteArray(0)

            while (bytesGenerated < outputKeyLength) {
                // RFC 5869: Counter must not exceed 255
                if (blockNumber > 255) {
                    throw IllegalStateException("HKDF output length limit exceeded")
                }

                // T(n) = HMAC-Hash(PRK, T(n-1) | info | n)
                hmacSha256.update(previousBlock)
                hmacSha256.update(contextBytes)
                hmacSha256.update(blockNumber.toByte())

                val currentBlock = hmacSha256.doFinal()

                // Copy needed bytes from current block
                val bytesToCopy = minOf(hashLen, outputKeyLength - bytesGenerated)
                currentBlock.copyInto(result, bytesGenerated, 0, bytesToCopy)

                bytesGenerated += bytesToCopy
                previousBlock = currentBlock
                blockNumber++
            }

            return result
        } catch (e: Exception) {
            throw IllegalStateException("HKDF generation failed", e)
        }
    }
}