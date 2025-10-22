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
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.main.remote.model.BrowserExtensionVaultDataV1Json
import com.twofasapp.data.main.remote.model.BrowserExtensionVaultDataV2Json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal class VaultDataForBrowserMapper(
    private val json: Json,
    private val itemMapper: ItemMapper,
    private val tagMapper: TagMapper,
) {

    fun mapToJsonV1(
        vaultBackup: VaultBackup,
        deviceId: String,
        encryptionKey: ByteArray,
    ): BrowserExtensionVaultDataV1Json {
        return with(vaultBackup) {
            BrowserExtensionVaultDataV1Json(
                logins = items.orEmpty()
                    .filter { it.securityType != SecurityType.Tier1 }
                    .map { item ->
                        val itemJson = itemMapper.mapToJsonV1(item)

                        if (item.securityType == SecurityType.Tier2) {
                            itemJson.copy(
                                deviceId = deviceId,
                                password = null,
                            )
                        } else {
                            itemJson.copy(
                                deviceId = deviceId,
                                password = itemJson.password?.let {
                                    encrypt(encryptionKey, it).encodeBase64()
                                },
                            )
                        }
                    },
                tags = tags.orEmpty().map { tagMapper.mapToJson(it) },
            )
        }
    }

    fun mapToJsonV2(
        vaultBackup: VaultBackup,
        deviceId: String,
        encryptionKey: ByteArray,
    ): BrowserExtensionVaultDataV2Json {
        return with(vaultBackup) {
            BrowserExtensionVaultDataV2Json(
                items = items.orEmpty()
                    .filter { it.securityType != SecurityType.Tier1 }
                    .filter { it.content !is ItemContent.Unknown }
                    .mapNotNull { item ->
                        val itemJson = itemMapper.mapToJson(item) ?: return@mapNotNull null

                        val contentJson =
                            itemJson.content
                                .processSecretFieldsKeys(
                                    secretField = { value ->
                                        if (item.securityType == SecurityType.Tier2) {
                                            null
                                        } else {
                                            encrypt(encryptionKey, value.toString()).encodeBase64()
                                        }
                                    },
                                )

                        itemJson.copy(
                            deviceId = deviceId,
                            content = contentJson,
                        )
                    },
                tags = tags.orEmpty().map { tagMapper.mapToJson(it) },
            )
        }
    }

    private fun JsonElement.processSecretFieldsKeys(
        secretField: (value: JsonElement) -> String?,
    ): JsonElement {
        val jsonObject = this as? JsonObject ?: return this

        val processedEntries = jsonObject.entries.map { (key, value) ->
            when {
                key.startsWith("s_") -> key to JsonPrimitive(secretField(value))
                else -> key to value
            }
        }

        return JsonObject(processedEntries.toMap())
    }
}