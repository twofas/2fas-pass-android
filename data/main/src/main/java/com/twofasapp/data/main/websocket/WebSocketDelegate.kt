/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.domain.ConnectedBrowser
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.remote.WebSocketInterface
import com.twofasapp.data.main.websocket.messages.IncomingMessageJson
import com.twofasapp.data.main.websocket.messages.OutgoingMessageJson
import com.twofasapp.data.main.websocket.messages.OutgoingPayloadJson
import com.twofasapp.data.main.websocket.messages.WebSocketException

internal interface WebSocketDelegate {
    var version: Int
    var expectedIncomingId: String

    val appBuild: AppBuild
    val device: Device
    val timeProvider: TimeProvider
    val connectedBrowsersRepository: ConnectedBrowsersRepository
    val itemsRepository: ItemsRepository
    val vaultCryptoScope: VaultCryptoScope
    val itemEncryptionMapper: ItemEncryptionMapper

    suspend fun WebSocketInterface.sendHelloMessage() {
        sendMessage(
            createOutgoingMessage(
                payload = OutgoingPayloadJson.Hello(
                    deviceId = device.uniqueId(),
                    deviceName = device.name(),
                    deviceOs = "android",
                    supportedFeatures = listOf(
                        "items.secureNote",
                    ),
                ),
            ),
        )
    }

    suspend fun WebSocketInterface.sendChallengeMessage(
        pkEpheMa: ByteArray,
        hkdfSalt: ByteArray,
    ) {
        sendMessage(
            createOutgoingMessage(
                payload = OutgoingPayloadJson.Challenge(
                    pkEpheMa = pkEpheMa.encodeBase64(),
                    hkdfSalt = hkdfSalt.encodeBase64(),
                ),
            ).also { expectedIncomingId = it.id },
        )
    }

    suspend fun WebSocketInterface.closeWithError(
        exception: Exception,
    ) {
        sendMessage(
            createOutgoingMessage(
                payload = OutgoingPayloadJson.CloseWithError(
                    errorCode = (exception as? WebSocketException)?.errorCode ?: 1000,
                    errorMessage = (exception as? WebSocketException)?.errorMessage ?: exception.message ?: "Unknown error.",
                ),
            ),
        )

        close()
    }

    suspend fun createOutgoingMessage(
        payload: OutgoingPayloadJson,
    ): OutgoingMessageJson {
        return OutgoingMessageJson(
            scheme = version,
            origin = device.name(),
            originVersion = appBuild.versionName,
            id = Uuid.generate(),
            action = when (payload) {
                is OutgoingPayloadJson.Hello -> "hello"
                is OutgoingPayloadJson.Challenge -> "challenge"
                is OutgoingPayloadJson.InitTransfer -> "initTransfer"
                is OutgoingPayloadJson.TransferChunk -> "transferChunk"
                is OutgoingPayloadJson.CloseWithSuccess -> "closeWithSuccess"
                is OutgoingPayloadJson.CloseWithError -> "closeWithError"
                is OutgoingPayloadJson.PullRequest -> "pullRequest"
                is OutgoingPayloadJson.PullRequestAction -> "pullRequestAction"
            },
            payload = payload,
        ).also { expectedIncomingId = it.id }
    }

    suspend fun saveBrowser(
        publicKey: ByteArray,
        message: IncomingMessageJson.Hello,
    ) {
        saveBrowser(
            browser = connectedBrowsersRepository.getBrowser(publicKey)
                ?: ConnectedBrowser.Empty.copy(publicKey = publicKey),
            message = message,
        )
    }

    suspend fun saveBrowser(
        browser: ConnectedBrowser,
        message: IncomingMessageJson.Hello,
    ) {
        val now = timeProvider.currentTimeUtc()

        val updatedConnectBrowser = browser.copy(
            browserName = message.payload.browserName,
            browserVersion = message.payload.browserVersion,
            extensionName = message.payload.browserExtName,
            lastSyncAt = now,
            createdAt = if (browser.createdAt == 0L) now else browser.createdAt,
        )

        connectedBrowsersRepository.updateBrowser(updatedConnectBrowser)
    }

    suspend fun saveBrowserSessionId(
        publicKey: ByteArray,
        sessionId: ByteArray,
    ) {
        connectedBrowsersRepository.getBrowser(publicKey)?.let { connectedBrowser ->
            saveBrowserSessionId(
                browser = connectedBrowser,
                sessionId = sessionId,
            )
        }
    }

    suspend fun saveBrowserSessionId(
        browser: ConnectedBrowser,
        sessionId: ByteArray,
    ) {
        connectedBrowsersRepository.updateBrowser(
            browser.copy(
                nextSessionId = sessionId,
            ),
        )
    }

    suspend fun getItem(id: String): Item? {
        val itemEncrypted = itemsRepository.getItem(id)
        if (itemEncrypted.deleted) return null

        return vaultCryptoScope.withVaultCipher(itemEncrypted.vaultId) {
            itemEncryptionMapper.decryptItem(
                itemEncrypted = itemEncrypted,
                vaultCipher = this,
                decryptSecretFields = true,
            )
        }
    }
}