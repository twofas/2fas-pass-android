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
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.data.main.domain.VaultBackup
import com.twofasapp.data.main.remote.model.BrowserExtensionVaultDataJson

internal class VaultDataForBrowserMapper(
    private val itemMapper: ItemMapper,
    private val tagMapper: TagMapper,
) {

    fun mapToJson(
        vaultBackup: VaultBackup,
        deviceId: String,
        encryptionKey: ByteArray,
    ): BrowserExtensionVaultDataJson {
        return with(vaultBackup) {
            BrowserExtensionVaultDataJson(
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
}