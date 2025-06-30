/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.crypto

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

object Gzip {
    fun compress(input: String): ByteArray {
        // Check if input is empty to avoid unnecessary processing
        if (input.isEmpty()) return ByteArray(0)

        // Initialize ByteArrayOutputStream to collect compressed bytes
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Use try-with-resources to automatically close GZIPOutputStream
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            // Convert String to bytes using UTF-8 encoding and write to GZIP stream
            gzipOutputStream.write(input.toByteArray(Charsets.UTF_8))
            // Ensure all data is flushed to the underlying stream
            gzipOutputStream.finish()
        }

        // Return the compressed data as ByteArray
        return byteArrayOutputStream.toByteArray()
    }
}