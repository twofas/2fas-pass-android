/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

data class ConnectedBrowser(
    val id: Int,
    val publicKey: ByteArray,
    val extensionName: String,
    val browserName: String,
    val browserVersion: String,
    val identicon: Identicon,
    val createdAt: Long,
    val lastSyncAt: Long,
    val nextSessionId: ByteArray,
) {
    companion object {
        val Empty = ConnectedBrowser(
            id = 0,
            publicKey = byteArrayOf(),
            extensionName = "",
            browserName = "",
            browserVersion = "",
            identicon = Identicon.Empty,
            createdAt = 0L,
            lastSyncAt = 0L,
            nextSessionId = byteArrayOf(),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectedBrowser

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (lastSyncAt != other.lastSyncAt) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (browserName != other.browserName) return false
        if (browserVersion != other.browserVersion) return false
        if (extensionName != other.extensionName) return false
        if (!nextSessionId.contentEquals(other.nextSessionId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + lastSyncAt.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + browserName.hashCode()
        result = 31 * result + browserVersion.hashCode()
        result = 31 * result + extensionName.hashCode()
        result = 31 * result + nextSessionId.contentHashCode()
        return result
    }
}