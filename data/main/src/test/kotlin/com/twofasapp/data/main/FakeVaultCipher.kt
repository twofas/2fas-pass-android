package com.twofasapp.data.main

import com.twofasapp.core.common.domain.crypto.EncryptedBytes

internal class FakeVaultCipher : VaultCipher {
    override fun encryptWithTrustedKey(string: String): EncryptedBytes = encode(TrustedPrefix, string)
    override fun encryptWithSecretKey(string: String): EncryptedBytes = encode(SecretPrefix, string)
    override fun encryptWithExternalKey(string: String): EncryptedBytes = encode(ExternalPrefix, string)

    override fun decryptWithTrustedKey(encryptedBytes: EncryptedBytes): String = decode(TrustedPrefix, encryptedBytes.bytes)
    override fun decryptWithSecretKey(encryptedBytes: EncryptedBytes): String = decode(SecretPrefix, encryptedBytes.bytes)
    override fun decryptWithExternalKey(encryptedBytes: EncryptedBytes): String = decode(ExternalPrefix, encryptedBytes.bytes)

    override fun decryptWithTrustedKey(encryptedBytes: ByteArray): String = decode(TrustedPrefix, encryptedBytes)
    override fun decryptWithSecretKey(encryptedBytes: ByteArray): String = decode(SecretPrefix, encryptedBytes)
    override fun decryptWithExternalKey(encryptedBytes: ByteArray): String = decode(ExternalPrefix, encryptedBytes)

    override fun isTrustedValid(): Boolean = true
    override fun isSecretValid(): Boolean = true
    override fun isExternalValid(): Boolean = true

    private fun encode(prefix: String, value: String): EncryptedBytes {
        return EncryptedBytes("$prefix:$value".toByteArray(Charsets.UTF_8))
    }

    private fun decode(prefix: String, bytes: ByteArray): String {
        val text = String(bytes, Charsets.UTF_8)
        return text.removePrefix("$prefix:")
    }

    private companion object {
        private const val SecretPrefix = "secret"
        private const val TrustedPrefix = "trusted"
        private const val ExternalPrefix = "external"
    }
}