/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeString
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.domain.VaultKeysExpiredException
import kotlinx.coroutines.Dispatchers

internal class VaultCipherImpl(
    private val androidKeyStore: AndroidKeyStore,
    private val vaultKeys: VaultKeys,
) : VaultCipher {

    private val keyTrusted: ByteArray? by lazy(Dispatchers.IO) { vaultKeys.trusted?.let { decrypt(androidKeyStore.appKey, it) } }
    private val keySecret: ByteArray? by lazy(Dispatchers.IO) { vaultKeys.secret?.let { decrypt(androidKeyStore.appKey, it) } }
    private val keyExternal: ByteArray? by lazy(Dispatchers.IO) { vaultKeys.external?.let { decrypt(androidKeyStore.appKey, it) } }

    override fun encryptWithTrustedKey(string: String) = string.encrypt(keyTrusted)
    override fun encryptWithSecretKey(string: String) = string.encrypt(keySecret)
    override fun encryptWithExternalKey(string: String) = string.encrypt(keyExternal)

    override fun decryptWithTrustedKey(encryptedBytes: EncryptedBytes) = encryptedBytes.decrypt(keyTrusted)
    override fun decryptWithSecretKey(encryptedBytes: EncryptedBytes) = encryptedBytes.decrypt(keySecret)
    override fun decryptWithExternalKey(encryptedBytes: EncryptedBytes): String = encryptedBytes.decrypt(keyExternal)

    override fun decryptWithTrustedKey(encryptedBytes: ByteArray) = EncryptedBytes(encryptedBytes).decrypt(keyTrusted)
    override fun decryptWithSecretKey(encryptedBytes: ByteArray) = EncryptedBytes(encryptedBytes).decrypt(keySecret)
    override fun decryptWithExternalKey(encryptedBytes: ByteArray): String = EncryptedBytes(encryptedBytes).decrypt(keyExternal)

    private fun EncryptedBytes.decrypt(key: ByteArray?): String {
        if (key == null) throw VaultKeysExpiredException()

        return decrypt(key, this).decodeString()
    }

    override fun isTrustedValid(): Boolean = keyTrusted != null
    override fun isSecretValid(): Boolean = keySecret != null
    override fun isExternalValid(): Boolean = keyExternal != null

    private fun String.encrypt(key: ByteArray?): EncryptedBytes {
        if (key == null) throw VaultKeysExpiredException()

        return encrypt(key, toByteArray())
    }
}