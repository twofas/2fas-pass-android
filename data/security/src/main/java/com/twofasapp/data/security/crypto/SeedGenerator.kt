/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.security.crypto

import com.twofasapp.core.common.crypto.WordList
import com.twofasapp.core.common.ktx.decodeHex
import com.twofasapp.core.common.ktx.encodeBinary
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.encodeHexCharToBinary
import com.twofasapp.core.common.ktx.sha256
import timber.log.Timber

class SeedGenerator {

    fun generate(entropy: ByteArray): Seed {
        // Compute sha256 from entropy
        val seed = entropy.sha256()

        // Take first 5 bits of the seed as a CRC:
        // - take first byte
        // - encode it to binary string
        // - take first 5 bits
        val crc = seed.first().encodeBinary().take(5)

        val entropyHex = entropy.encodeHex()
        val seedHex = seed.encodeHex()

        // Convert entropy to binary strings (4 bits per char) + add CRC at the end
        val entropyBinary4Bits = entropyHex.map { it.encodeHexCharToBinary() } + crc

        // Group 4-bit binary strings to 11-bit binary strings
        val entropyBinary11Bits = entropyBinary4Bits.joinToString("").chunked(11)

        // Convert to decimals
        val entropyDecimals = entropyBinary11Bits.map { it.toInt(radix = 2) }

        // Convert decimals to words
        val entropyWords = entropyDecimals.map { WordList.words[it].lowercase() }

        Timber.i(
            buildString {
                appendLine("[GENERATE SEED]")
                appendLine("entropyBinary4Bits = $entropyBinary4Bits")
                appendLine("entropyBinary11Bits = $entropyBinary11Bits")
                appendLine("entropyDecimals = $entropyDecimals")
                appendLine("entropy = $entropyHex")
                appendLine("words = $entropyWords (size=${entropyWords.size})")
                appendLine("seed = $seedHex")
                appendLine("[END]")
            },
        )

        return Seed(
            words = entropyWords,
            entropyHex = entropyHex,
            seedHex = seedHex,
        )
    }

    fun restore(words: List<String>): Seed {
        val entropyDecimals = words.map { it.lowercase() }.map { WordList.words.indexOf(it) }
        val entropyBinary11Bits = entropyDecimals.joinToString("") { it.toString(radix = 2).padStart(11, '0') }
        val crc = entropyBinary11Bits.takeLast(5)
        val entropyBinary4Bits = entropyBinary11Bits.dropLast(5).chunked(4)
        val entropyHex = entropyBinary4Bits.joinToString("") { it.toInt(2).toString(16) }
        val seed = entropyHex.decodeHex().sha256()
        val seedHex = seed.encodeHex()

        if (crc != seed.first().encodeBinary().take(5)) {
            throw IllegalArgumentException("Invalid word list! (CRC error)")
        }

        Timber.i(
            buildString {
                appendLine("[RESTORE SEED]")
                appendLine("entropy = $entropyHex")
                appendLine("words = $words (size=${words.size})")
                appendLine("seed = $seedHex")
                appendLine("[END]")
            },
        )

        return Seed(
            words = words,
            entropyHex = entropyHex,
            seedHex = seedHex,
        )
    }
}