/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

fun Char.encodeHexCharToBinary(padStart: Int = 4): String {
    return digitToInt(16).toString(2).padStart(4, '0')
}

fun Byte.encodeBinary(): String {
    return toInt().and(0xFF).toString(2).padStart(8, '0')
}

fun ByteArray.encodeBinary(): String {
    return joinToString("") { byte -> byte.encodeBinary() }
}