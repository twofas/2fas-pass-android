/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import java.security.SecureRandom
import kotlin.math.max

object PasswordGenerator {
    private val digitsPool = ('0'..'9').toList()
    private val uppercasePool = ('A'..'Z').toList()
    private val specialPool = listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_')
    private val lowercasePool = ('a'..'z').toList()

    fun generatePassword(
        settings: PasswordGeneratorSettings,
    ): String {
        val secureRandom = SecureRandom()

        val minDigits = if (settings.requireDigits) {
            when (settings.length) {
                in 6..8 -> 2
                in 9..12 -> randomInt(secureRandom, 2, 3)
                in 13..16 -> randomInt(secureRandom, 3, 4)
                in 17..24 -> randomInt(secureRandom, 3, 6)
                else -> randomInt(secureRandom, 3, 8)
            }
        } else {
            0
        }

        val minUppercase = if (settings.requireUppercase) {
            when (settings.length) {
                in 6..8 -> 1
                in 9..12 -> randomInt(secureRandom, 1, 2)
                in 13..16 -> randomInt(secureRandom, 2, 3)
                in 17..24 -> randomInt(secureRandom, 2, 4)
                else -> randomInt(secureRandom, 3, 6)
            }
        } else {
            0
        }

        val minSpecial = if (settings.requireSpecial) {
            when (settings.length) {
                in 6..8 -> 1
                in 9..12 -> randomInt(secureRandom, 1, 2)
                in 13..16 -> randomInt(secureRandom, 2, 3)
                in 17..24 -> randomInt(secureRandom, 2, 4)
                else -> randomInt(secureRandom, 3, 6)
            }
        } else {
            0
        }

        val digits = generateRandomCharacters(secureRandom, minDigits, digitsPool)
        val uppercase = generateRandomCharacters(secureRandom, minUppercase, uppercasePool)
        val special = generateRandomCharacters(secureRandom, minSpecial, specialPool)
        val lowercase = generateRandomCharacters(secureRandom, max(0, settings.length - minDigits - minUppercase - minSpecial), lowercasePool)

        return (digits + uppercase + special + lowercase).toList().shuffled().joinToString("")
    }

    private fun generateRandomCharacters(secureRandom: SecureRandom, count: Int, pool: List<Char>): String {
        return (1..count)
            .map { pool[secureRandom.nextInt(pool.size)] }
            .joinToString("")
    }

    private fun randomInt(secureRandom: SecureRandom, from: Int, to: Int): Int {
        return secureRandom.nextInt(to - from + 1) + from
    }
}