/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.emptyEncryptedBytes

internal class VaultCipherEmpty : VaultCipher {
    override fun encryptWithTrustedKey(string: String) = emptyEncryptedBytes()
    override fun encryptWithSecretKey(string: String) = emptyEncryptedBytes()
    override fun encryptWithExternalKey(string: String) = emptyEncryptedBytes()
    override fun decryptWithTrustedKey(encryptedBytes: EncryptedBytes) = ""
    override fun decryptWithSecretKey(encryptedBytes: EncryptedBytes) = ""
    override fun decryptWithExternalKey(encryptedBytes: EncryptedBytes) = ""
    override fun decryptWithTrustedKey(encryptedBytes: ByteArray) = ""
    override fun decryptWithSecretKey(encryptedBytes: ByteArray) = ""
    override fun decryptWithExternalKey(encryptedBytes: ByteArray) = ""
    override fun isTrustedValid(): Boolean = false
    override fun isSecretValid(): Boolean = false
    override fun isExternalValid(): Boolean = false
}