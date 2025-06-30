/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

fun Byte.encodeHex(): String {
    return "%02x".format(this)
}

fun ByteArray.encodeHex(): String {
    return joinToString("") { byte -> byte.encodeHex() }
}

fun List<Byte>.encodeHex(): String {
    return joinToString("") { byte -> byte.encodeHex() }
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}