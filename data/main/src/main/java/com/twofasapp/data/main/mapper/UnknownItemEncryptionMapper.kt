/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.data.main.VaultCipher
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class UnknownItemEncryptionMapper(
    private val json: Json,
) {
    fun encrypt(
        rawJson: String,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): String {
        val jsonElement = runCatching { json.parseToJsonElement(rawJson) }.getOrNull()
        if (jsonElement !is JsonObject) {
            return rawJson
        }

        val processed = jsonElement.mapValues { (key, value) ->
            if (key.startsWith(SecretFieldPrefix)) {
                encryptValue(value, securityType, vaultCipher)
            } else {
                value
            }
        }

        return runCatching { json.encodeToString(JsonObject(processed)) }.getOrElse { rawJson }
    }

    fun decrypt(
        rawJson: String,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
        decryptSecretFields: Boolean,
    ): ItemContent {
        if (!decryptSecretFields) {
            return ItemContent.Unknown(rawJson = rawJson)
        }

        val jsonElement = runCatching { json.parseToJsonElement(rawJson) }.getOrNull()
        if (jsonElement !is JsonObject) {
            return ItemContent.Unknown(rawJson = rawJson)
        }

        val processed = jsonElement.mapValues { (key, value) ->
            if (key.startsWith(SecretFieldPrefix)) {
                decryptValue(value, securityType, vaultCipher)
            } else {
                value
            }
        }

        val decryptedJson = runCatching { json.encodeToString(JsonObject(processed)) }.getOrElse { rawJson }
        return ItemContent.Unknown(rawJson = decryptedJson)
    }

    fun encryptSecretFields(
        rawJson: String,
        encryptionKey: ByteArray,
    ): ItemContent.Unknown {
        val jsonElement = runCatching { json.parseToJsonElement(rawJson) }.getOrNull()
        if (jsonElement !is JsonObject) {
            return ItemContent.Unknown(rawJson = rawJson)
        }

        val processed = jsonElement.mapValues { (key, value) ->
            if (key.startsWith(SecretFieldPrefix)) {
                encryptValueWithKey(value, encryptionKey)
            } else {
                value
            }
        }

        val encryptedJson = runCatching { json.encodeToString(JsonObject(processed)) }.getOrElse { rawJson }
        return ItemContent.Unknown(rawJson = encryptedJson)
    }

    private fun encryptValue(
        value: JsonElement,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): JsonElement {
        if (value === JsonNull) return JsonNull
        val primitive = value as? JsonPrimitive ?: return value
        if (!primitive.isString) return value

        val encrypted = runCatching {
            encryptString(
                value = primitive.content,
                securityType = securityType,
                vaultCipher = vaultCipher,
            )
        }.getOrNull() ?: return value

        return JsonPrimitive(encrypted)
    }

    private fun encryptValueWithKey(
        value: JsonElement,
        encryptionKey: ByteArray,
    ): JsonElement {
        if (value === JsonNull) return JsonNull
        val primitive = value as? JsonPrimitive ?: return value
        if (!primitive.isString) return value

        val encrypted = runCatching {
            encrypt(key = encryptionKey, data = primitive.content).encodeBase64()
        }.getOrNull() ?: return value

        return JsonPrimitive(encrypted)
    }

    private fun decryptValue(
        value: JsonElement,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): JsonElement {
        if (value === JsonNull) return JsonNull
        val primitive = value as? JsonPrimitive ?: return value
        if (!primitive.isString) return value
        val encoded = primitive.content
        if (encoded.isBlank()) return value

        val decrypted = runCatching {
            decryptString(
                encoded = encoded,
                securityType = securityType,
                vaultCipher = vaultCipher,
            )
        }.getOrNull() ?: return value

        return JsonPrimitive(decrypted)
    }

    private fun encryptString(
        value: String,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): String {
        val encrypted = when (securityType) {
            SecurityType.Tier1 -> vaultCipher.encryptWithSecretKey(value)
            SecurityType.Tier2 -> vaultCipher.encryptWithSecretKey(value)
            SecurityType.Tier3 -> vaultCipher.encryptWithTrustedKey(value)
        }

        return encrypted.encodeBase64()
    }

    private fun decryptString(
        encoded: String,
        securityType: SecurityType,
        vaultCipher: VaultCipher,
    ): String {
        val encryptedBytes = EncryptedBytes(encoded.decodeBase64())

        return when (securityType) {
            SecurityType.Tier1 -> vaultCipher.decryptWithSecretKey(encryptedBytes)
            SecurityType.Tier2 -> vaultCipher.decryptWithSecretKey(encryptedBytes)
            SecurityType.Tier3 -> vaultCipher.decryptWithTrustedKey(encryptedBytes)
        }
    }

    private companion object {
        private const val SecretFieldPrefix = "s_"
    }
}