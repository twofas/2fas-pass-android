/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.ktx.decodeString
import com.twofasapp.data.main.domain.ConnectedBrowser
import com.twofasapp.data.main.domain.IdenticonGenerator
import com.twofasapp.data.main.local.model.ConnectedBrowserEntity
import java.security.Key

internal class ConnectedBrowserMapper {

    fun mapToDomain(entity: ConnectedBrowserEntity, appKey: Key): ConnectedBrowser {
        val publicKeyDecrypted = decrypt(appKey, entity.publicKey)
        return with(entity) {
            ConnectedBrowser(
                id = id,
                publicKey = publicKeyDecrypted,
                extensionName = decrypt(appKey, extensionName).decodeString(),
                browserName = decrypt(appKey, browserName).decodeString(),
                browserVersion = decrypt(appKey, browserVersion).decodeString(),
                identicon = IdenticonGenerator.generate(publicKeyDecrypted),
                createdAt = createdAt,
                lastSyncAt = lastSyncAt,
                nextSessionId = decrypt(appKey, nextSessionId),
            )
        }
    }

    fun mapToEntity(domain: ConnectedBrowser, appKey: Key): ConnectedBrowserEntity {
        return with(domain) {
            ConnectedBrowserEntity(
                id = id,
                publicKey = encrypt(appKey, publicKey),
                extensionName = encrypt(appKey, extensionName),
                browserName = encrypt(appKey, browserName),
                browserVersion = encrypt(appKey, browserVersion),
                createdAt = createdAt,
                lastSyncAt = lastSyncAt,
                nextSessionId = encrypt(appKey, nextSessionId),
            )
        }
    }
}