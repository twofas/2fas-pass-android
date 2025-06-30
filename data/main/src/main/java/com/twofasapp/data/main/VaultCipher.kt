/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

interface VaultCipher {
    fun encryptWithTrustedKey(string: String): EncryptedBytes
    fun encryptWithSecretKey(string: String): EncryptedBytes
    fun encryptWithExternalKey(string: String): EncryptedBytes

    fun decryptWithTrustedKey(encryptedBytes: EncryptedBytes): String
    fun decryptWithSecretKey(encryptedBytes: EncryptedBytes): String
    fun decryptWithExternalKey(encryptedBytes: EncryptedBytes): String

    fun decryptWithTrustedKey(encryptedBytes: ByteArray): String
    fun decryptWithSecretKey(encryptedBytes: ByteArray): String
    fun decryptWithExternalKey(encryptedBytes: ByteArray): String

    fun isTrustedValid(): Boolean
    fun isSecretValid(): Boolean
    fun isExternalValid(): Boolean
}