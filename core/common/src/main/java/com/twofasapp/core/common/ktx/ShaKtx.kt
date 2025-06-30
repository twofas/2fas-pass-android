/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

import java.security.MessageDigest

fun ByteArray.sha256(): ByteArray {
    return hash(this, "SHA-256")
}

private fun hash(input: ByteArray, algorithm: String): ByteArray {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input)
}