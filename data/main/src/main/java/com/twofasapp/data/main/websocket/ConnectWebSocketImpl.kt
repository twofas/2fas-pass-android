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
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.EcKeyConverter
import com.twofasapp.core.common.crypto.HkdfGenerator
import com.twofasapp.core.common.crypto.RandomGenerator
import com.twofasapp.core.common.crypto.SharedKeyGenerator
import com.twofasapp.core.common.crypto.SignatureVerifier
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeByteArray
import com.twofasapp.core.common.ktx.sha256
import com.twofasapp.core.common.push.PushTokenProvider
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.ConnectWebSocketResult
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.remote.BrowserRequestsRemoteSource
import com.twofasapp.data.main.websocket.messages.IncomingMessageJson
import com.twofasapp.data.main.websocket.messages.OutgoingPayloadJson
import com.twofasapp.data.main.websocket.messages.WebSocketException
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.purchases.domain.SubscriptionPlan
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.CancellationException

internal class ConnectWebSocketImpl(
    override val appBuild: AppBuild,
    override val device: Device,
    override val timeProvider: TimeProvider,
    override val connectedBrowsersRepository: ConnectedBrowsersRepository,
    override val itemsRepository: ItemsRepository,
    override val vaultCryptoScope: VaultCryptoScope,
    override val loginDecryptionMapper: ItemEncryptionMapper,
    private val dispatchers: Dispatchers,
    private val androidKeyStore: AndroidKeyStore,
    private val pushTokenProvider: PushTokenProvider,
    private val browserRequestsRemoteSource: BrowserRequestsRemoteSource,
    private val backupRepository: BackupRepository,
    private val vaultsRepository: VaultsRepository,
    private val purchasesRepository: PurchasesRepository,
) : ConnectWebSocket, WebSocketDelegate {

    override var expectedIncomingId: String = ""
    private var error: Exception? = null
    private val chunks = mutableListOf<String>()
    private val chunkSize = 2 * 1024 * 1024

    override suspend fun open(
        connectData: ConnectData,
    ): ConnectWebSocketResult {
        try {
            withContext(dispatchers.io) {
                Timber.d("Connect: $connectData")

                val epheMa = androidKeyStore.generateConnectEphemeralEcKey()
                val pkEpheMa = epheMa.public.encoded
                val pkEpheBe = EcKeyConverter.createPublicKey(connectData.pkEpheBe)
                val hkdfSalt = RandomGenerator.generate(16)
                val newSessionId = RandomGenerator.generate(16)

                val sharedSecretEcdh = SharedKeyGenerator.generate(
                    skEpheMa = epheMa.private,
                    pkEpheBe = pkEpheBe,
                )

                val sessionKey = HkdfGenerator.generate(
                    inputKeyMaterial = sharedSecretEcdh,
                    salt = hkdfSalt,
                    contextInfo = "SessionKey",
                )

                val dataKey = HkdfGenerator.generate(
                    inputKeyMaterial = sessionKey,
                    salt = hkdfSalt,
                    contextInfo = "Data",
                )

                clearConnection()

                browserRequestsRemoteSource.openWebSocket(
                    sessionIdHex = connectData.sessionId,
                    onOpened = {
                        try {
                            SignatureVerifier.verify(
                                key = connectData.pkPersBe,
                                data = connectData.data,
                                signature = connectData.signature,
                            )

                            sendHelloMessage()
                        } catch (e: Exception) {
                            error = WebSocketException(1100, "Signature could not be verified.")
                            closeWithError(error!!)
                        }
                    },
                    onMessageReceived = { message ->
                        try {
                            if (message == null) {
                                throw WebSocketException(1003, "Message not supported.")
                            }

                            if (message.id != expectedIncomingId) {
                                throw WebSocketException(1001, "Message identifier could not be verified.")
                            }

                            when (message) {
                                is IncomingMessageJson.Hello -> {
                                    saveBrowser(
                                        publicKey = connectData.pkPersBe,
                                        message = message,
                                    )

                                    sendChallengeMessage(
                                        pkEpheMa = pkEpheMa,
                                        hkdfSalt = hkdfSalt,
                                    )
                                }

                                is IncomingMessageJson.Challenge -> {
                                    try {
                                        decrypt(
                                            key = sessionKey,
                                            data = EncryptedBytes(
                                                bytes = message.payload.hkdfSaltEnc.decodeBase64(),
                                            ),
                                        )

                                        val newSessionIdEnc = encrypt(
                                            key = dataKey,
                                            data = newSessionId,
                                        )

                                        val passT3Key = HkdfGenerator.generate(
                                            inputKeyMaterial = sessionKey,
                                            salt = hkdfSalt,
                                            contextInfo = "PassT3",
                                        )

                                        val fcmTokenEnc = encrypt(
                                            key = dataKey,
                                            data = pushTokenProvider.provide(),
                                        )

                                        val expirationDateEnc = when (val plan = purchasesRepository.getSubscriptionPlan()) {
                                            is SubscriptionPlan.Free -> null
                                            is SubscriptionPlan.Paid -> {
                                                plan.expirationDate?.let { expirationDate ->
                                                    encrypt(
                                                        key = dataKey,
                                                        data = expirationDate.toEpochMilli().toString(),
                                                    )
                                                }
                                            }
                                        }

                                        val vaultDataGzip = backupRepository.createCompressedVaultDataForBrowserExtension(
                                            vaultId = vaultsRepository.getVault().id,
                                            deviceId = device.uniqueId(),
                                            encryptionPassKey = passT3Key,
                                        )

                                        val vaultDataGzipEnc = encrypt(
                                            key = dataKey,
                                            data = vaultDataGzip,
                                        )

                                        val sha256EncGzipVaultData = vaultDataGzipEnc.bytes.sha256()

                                        chunks.clear()

                                        vaultDataGzipEnc
                                            .encodeBase64()
                                            .chunked(chunkSize)
                                            .forEach { chunk ->
                                                chunks.add(chunk)
                                            }

                                        val totalChunks = chunks.size
                                        val totalSize = vaultDataGzip.encodeByteArray().size // gzipped JSON before encryption

                                        sendMessage(
                                            createOutgoingMessage(
                                                OutgoingPayloadJson.InitTransfer(
                                                    totalChunks = totalChunks,
                                                    totalSize = totalSize,
                                                    sha256GzipVaultDataEnc = sha256EncGzipVaultData.encodeBase64(),
                                                    fcmTokenEnc = fcmTokenEnc.encodeBase64(),
                                                    newSessionIdEnc = newSessionIdEnc.encodeBase64(),
                                                    expirationDateEnc = expirationDateEnc?.encodeBase64(),
                                                ),
                                            ),
                                        )
                                    } catch (e: Exception) {
                                        throw WebSocketException(1300, "Error when calculating challenge (${e.message})")
                                    }
                                }

                                is IncomingMessageJson.InitTransferConfirmed -> {
                                    sendMessage(
                                        createOutgoingMessage(
                                            payload = OutgoingPayloadJson.TransferChunk(
                                                chunkIndex = 0,
                                                chunkSize = chunks[0].length,
                                                chunkData = chunks[0],
                                            ),
                                        ),
                                    )
                                }

                                is IncomingMessageJson.TransferChunkConfirmed -> {
                                    try {
                                        val chunkIndex = message.payload.chunkIndex + 1
                                        sendMessage(
                                            createOutgoingMessage(
                                                payload = OutgoingPayloadJson.TransferChunk(
                                                    chunkIndex = chunkIndex,
                                                    chunkSize = chunks[chunkIndex].length,
                                                    chunkData = chunks[chunkIndex],
                                                ),
                                            ),
                                        )
                                    } catch (e: Exception) {
                                        throw WebSocketException(1600, "Error when sending chunk.")
                                    }
                                }

                                is IncomingMessageJson.TransferCompleted -> {
                                    saveBrowserSessionId(
                                        publicKey = connectData.pkPersBe,
                                        sessionId = newSessionId,
                                    )

                                    sendMessage(
                                        createOutgoingMessage(
                                            payload = OutgoingPayloadJson.CloseWithSuccess,
                                        ),
                                    )
                                }

                                is IncomingMessageJson.CloseWithError -> {
                                    throw WebSocketException(
                                        errorCode = message.payload.errorCode,
                                        errorMessage = message.payload.errorMessage,
                                    )
                                }

                                is IncomingMessageJson.Unknown -> {
                                    throw WebSocketException(1005, "Unknown websocket message received.")
                                }

                                else -> Unit
                            }
                        } catch (e: Exception) {
                            error = e
                            closeWithError(e)
                        }
                    },
                )
            }

            return if (error != null) {
                ConnectWebSocketResult.Failure(
                    errorCode = (error as? WebSocketException)?.errorCode ?: 1000,
                    errorMessage = (error as? WebSocketException)?.errorMessage ?: error!!.message ?: "Unknown error.",
                )
            } else {
                ConnectWebSocketResult.Success
            }
        } catch (e: CancellationException) {
            return ConnectWebSocketResult.Failure(1000, "Browser extension has been closed.")
        } catch (e: Exception) {
            return ConnectWebSocketResult.Failure(1000, e.message ?: "Unknown error.")
        }
    }

    private fun clearConnection() {
        expectedIncomingId = ""
        error = null
        chunks.clear()
    }
}