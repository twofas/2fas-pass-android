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
    private val loginMapper: LoginMapper,
    private val tagMapper: TagMapper,
) {

    fun mapToJson(
        vaultBackup: VaultBackup,
        deviceId: String,
        encryptionKey: ByteArray,
    ): BrowserExtensionVaultDataJson {
        return with(vaultBackup) {
            BrowserExtensionVaultDataJson(
                logins = logins.orEmpty()
                    .filter { it.securityType != SecurityType.Tier1 }
                    .map { login ->
                        val loginJson = loginMapper.mapToJson(login)

                        if (login.securityType == SecurityType.Tier2) {
                            loginJson.copy(
                                deviceId = deviceId,
                                password = null,
                            )
                        } else {
                            loginJson.copy(
                                deviceId = deviceId,
                                password = loginJson.password?.let {
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