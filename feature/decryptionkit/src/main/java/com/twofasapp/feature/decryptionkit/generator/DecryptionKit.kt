/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.decryptionkit.generator

import androidx.core.net.toUri
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeUrlParam
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeUrlParam

data class DecryptionKit(
    val words: List<String>,
    val entropy: ByteArray,
    val masterKey: ByteArray?,
) {
    companion object {
        val Empty = DecryptionKit(words = emptyList(), entropy = byteArrayOf(), masterKey = byteArrayOf())

        fun readQrCodeContent(content: String): DecryptionKit {
            val uri = content.toUri()

            return DecryptionKit(
                words = emptyList(),
                entropy = uri.getQueryParameter("entropy")?.decodeUrlParam()?.decodeBase64()
                    ?: throw RuntimeException("Invalid QR Code - entropy key is missing"),
                masterKey = uri.getQueryParameter("master_key")?.decodeUrlParam()?.decodeBase64(),
            )
        }
    }

    fun generateQrCodeContent(includeMasterKey: Boolean): String {
        return buildString {
            append("twopass://recovery-kit?")
            append("entropy=${entropy.encodeBase64().encodeUrlParam()}")

            if (includeMasterKey && masterKey != null) {
                append("&master_key=${masterKey.encodeBase64().encodeUrlParam()}")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecryptionKit

        if (words != other.words) return false
        if (!entropy.contentEquals(other.entropy)) return false
        if (masterKey != null) {
            if (other.masterKey == null) return false
            if (!masterKey.contentEquals(other.masterKey)) return false
        } else if (other.masterKey != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = words.hashCode()
        result = 31 * result + entropy.contentHashCode()
        result = 31 * result + (masterKey?.contentHashCode() ?: 0)
        return result
    }
}