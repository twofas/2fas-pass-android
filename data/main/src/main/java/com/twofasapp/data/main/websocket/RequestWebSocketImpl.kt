/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket

import android.app.NotificationManager
import androidx.core.net.toUri
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.EcKeyConverter
import com.twofasapp.core.common.crypto.HkdfGenerator
import com.twofasapp.core.common.crypto.RandomGenerator
import com.twofasapp.core.common.crypto.SharedKeyGenerator
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.PasswordGenerator
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeString
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeByteArray
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.domain.BrowserRequestAction
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.BrowserRequestResponse
import com.twofasapp.data.main.domain.RequestWebSocketResult
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.ItemMapper
import com.twofasapp.data.main.mapper.ItemSecurityTypeMapper
import com.twofasapp.data.main.mapper.UriMatcherMapper
import com.twofasapp.data.main.remote.BrowserRequestsRemoteSource
import com.twofasapp.data.main.websocket.messages.BrowserRequestActionJson
import com.twofasapp.data.main.websocket.messages.IncomingMessageJson
import com.twofasapp.data.main.websocket.messages.OutgoingPayloadJson
import com.twofasapp.data.main.websocket.messages.WebSocketException
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import timber.log.Timber
import java.util.concurrent.CancellationException

internal class RequestWebSocketImpl(
    override val appBuild: AppBuild,
    override val device: Device,
    override val timeProvider: TimeProvider,
    override val connectedBrowsersRepository: ConnectedBrowsersRepository,
    override val itemsRepository: ItemsRepository,
    override val vaultCryptoScope: VaultCryptoScope,
    override val loginDecryptionMapper: ItemEncryptionMapper,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: Dispatchers,
    private val androidKeyStore: AndroidKeyStore,
    private val json: Json,
    private val browserRequestsRemoteSource: BrowserRequestsRemoteSource,
    private val notificationManager: NotificationManager,
    private val itemMapper: ItemMapper,
    private val itemSecurityTypeMapper: ItemSecurityTypeMapper,
    private val uriMatcherMapper: UriMatcherMapper,
) : RequestWebSocket, WebSocketDelegate {

    override var expectedIncomingId: String = ""
    private var error: Exception? = null

    override suspend fun open(
        requestData: BrowserRequestData,
        onBrowserRequestAction: suspend (BrowserRequestAction) -> BrowserRequestResponse,
    ): RequestWebSocketResult {
        try {
            withContext(dispatchers.io) {
                Timber.d("Request: $requestData")

                val epheMa = androidKeyStore.generateConnectEphemeralEcKey()
                val pkEpheMa = epheMa.public.encoded
                val pkEpheBe = EcKeyConverter.createPublicKey(requestData.pkEpheBe)
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

                val sessionId = requestData.browser.nextSessionId.encodeHex()

                clearConnection()

                browserRequestsRemoteSource.openWebSocket(
                    sessionIdHex = sessionId,
                    onOpened = { sendHelloMessage() },
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
                                        browser = requestData.browser,
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

                                        sendMessage(
                                            createOutgoingMessage(
                                                OutgoingPayloadJson.PullRequest(
                                                    newSessionIdEnc = newSessionIdEnc.encodeBase64(),
                                                ),
                                            ),
                                        )
                                    } catch (e: Exception) {
                                        throw WebSocketException(1300, "Error when calculating challenge (${e.message})")
                                    }
                                }

                                is IncomingMessageJson.PullRequest -> {
                                    try {
                                        val request = decrypt(
                                            key = dataKey,
                                            data = EncryptedBytes(
                                                bytes = message.payload.dataEnc.decodeBase64(),
                                            ),
                                        )

                                        val action = json.decodeFromString<BrowserRequestActionJson>(request.decodeString())
                                            .asDomain(
                                                hkdfSalt = hkdfSalt,
                                                sessionKey = sessionKey,
                                            )

                                        val response = onBrowserRequestAction(action)

                                        val responseData = createRequestResponseData(
                                            hkdfSalt = hkdfSalt,
                                            sessionKey = sessionKey,
                                            action = action,
                                            response = response,
                                        )

                                        Timber.d(responseData)

                                        val responseDataEnc = encrypt(
                                            key = dataKey,
                                            data = responseData.encodeByteArray(),
                                        )

                                        sendMessage(
                                            createOutgoingMessage(
                                                OutgoingPayloadJson.PullRequestAction(
                                                    dataEnc = responseDataEnc.encodeBase64(),
                                                ),
                                            ),
                                        )

                                        notificationManager.cancel(216633)
                                    } catch (e: WebSocketException) {
                                        throw e
                                    } catch (e: Exception) {
                                        throw WebSocketException(1500, "Error when generating request data (${e.message})")
                                    }
                                }

                                is IncomingMessageJson.PullRequestCompleted -> {
                                    saveBrowserSessionId(
                                        browser = requestData.browser,
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
                RequestWebSocketResult.Failure(
                    errorCode = (error as? WebSocketException)?.errorCode ?: 1000,
                    errorMessage = (error as? WebSocketException)?.errorMessage ?: error!!.message ?: "Unknown error.",
                )
            } else {
                RequestWebSocketResult.Success
            }
        } catch (e: CancellationException) {
            return RequestWebSocketResult.Failure(1000, "Browser extension request is no longer valid.")
        } catch (e: Exception) {
            return RequestWebSocketResult.Failure(1000, e.message ?: "Unknown error.")
        }
    }

    private suspend fun BrowserRequestActionJson.asDomain(
        hkdfSalt: ByteArray,
        sessionKey: ByteArray,
    ): BrowserRequestAction {
        Timber.d(this.toString())

        return when (this) {
            is BrowserRequestActionJson.PasswordRequest -> {
                BrowserRequestAction.PasswordRequest(
                    type = type,
                    item = getItem(data.itemId) ?: throw WebSocketException(1501, "Could not find requested item."),
                )
            }

            is BrowserRequestActionJson.DeleteLogin -> {
                BrowserRequestAction.DeleteLogin(
                    type = type,
                    item = getItem(data.itemId) ?: throw WebSocketException(1501, "Could not find requested item."),
                )
            }

            is BrowserRequestActionJson.AddLogin -> {
                val newItem = Item.create(
                    contentType = "login",
                    content = ItemContent.Login.Empty.copy(
                        name = (data.url.toUri().host ?: data.url).removePrefix("www."),
                        uris = listOf(ItemUri(text = data.url)),
                        username = if (data.usernamePasswordMobile == true) {
                            itemsRepository.getMostCommonUsernames().firstOrNull().orEmpty()
                        } else {
                            data.username
                        },
                        password = if (data.usernamePasswordMobile == true) {
                            SecretField.ClearText(
                                PasswordGenerator.generatePassword(
                                    settingsRepository.observePasswordGeneratorSettings().first(),
                                ),
                            )
                        } else {
                            data.passwordEnc?.let { encryptedPassword ->
                                val newPasswordKey = HkdfGenerator.generate(
                                    inputKeyMaterial = sessionKey,
                                    salt = hkdfSalt,
                                    contextInfo = "PassNew",
                                )

                                val password = decrypt(
                                    key = newPasswordKey,
                                    data = EncryptedBytes(encryptedPassword.decodeBase64()),
                                )

                                SecretField.ClearText(password.decodeString())
                            }
                        },
                    ),
                )

                BrowserRequestAction.AddLogin(
                    type = type,
                    item = newItem,
                )
            }

            is BrowserRequestActionJson.UpdateLogin -> {
                val item = getItem(data.id)
                    ?: throw WebSocketException(1501, "Could not find requested item.")

                BrowserRequestAction.UpdateLogin(
                    type = type,
                    item = item,
                    updatedItem = item.copy(
                        securityType = data.securityType.let(itemSecurityTypeMapper::mapToDomainFromJson),
                        content = (item.content as ItemContent.Login).let { content ->
                            content.copy(
                                name = data.name ?: content.name,
                                username = if (data.usernameMobile == true) {
                                    itemsRepository.getMostCommonUsernames().firstOrNull().orEmpty()
                                } else {
                                    data.username ?: content.username
                                },
                                password = if (data.passwordMobile == true) {
                                    SecretField.ClearText(
                                        PasswordGenerator.generatePassword(
                                            settingsRepository.observePasswordGeneratorSettings().first(),
                                        ),
                                    )
                                } else {
                                    data.passwordEnc?.let { encryptedPassword ->
                                        val updatePasswordKey = HkdfGenerator.generate(
                                            inputKeyMaterial = sessionKey,
                                            salt = hkdfSalt,
                                            contextInfo = when (data.securityType.let(itemSecurityTypeMapper::mapToDomainFromJson)) {
                                                SecurityType.Tier1 -> "PassT1"
                                                SecurityType.Tier2 -> "PassT2"
                                                SecurityType.Tier3 -> "PassT3"
                                            },
                                        )

                                        val password = decrypt(
                                            key = updatePasswordKey,
                                            data = EncryptedBytes(encryptedPassword.decodeBase64()),
                                        )

                                        SecretField.ClearText(password.decodeString())
                                    } ?: content.password
                                },
                                notes = data.notes ?: content.notes,
                                uris = data.uris?.map { uri ->
                                    ItemUri(
                                        text = uri.text,
                                        matcher = uriMatcherMapper.mapToDomainFromJson(uri.matcher),
                                    )
                                } ?: content.uris,
                            )
                        },
                    ),
                )
            }

            is BrowserRequestActionJson.Unknown -> {
                throw WebSocketException(1502, "Unknown request action.")
            }
        }
    }

    private suspend fun createRequestResponseData(
        hkdfSalt: ByteArray,
        sessionKey: ByteArray,
        action: BrowserRequestAction,
        response: BrowserRequestResponse,
    ): String {
        val deviceId = device.uniqueId()
        val type = action.type
        val status = when (response) {
            is BrowserRequestResponse.PasswordRequestAccept -> "accept"
            is BrowserRequestResponse.DeleteLoginAccept -> "accept"
            is BrowserRequestResponse.AddLoginAccept -> {
                when (response.item.securityType) {
                    SecurityType.Tier1 -> "addedInT1"
                    SecurityType.Tier2 -> "added"
                    SecurityType.Tier3 -> "added"
                }
            }

            is BrowserRequestResponse.UpdateLoginAccept -> {
                when (response.item.securityType) {
                    SecurityType.Tier1 -> "addedInT1"
                    SecurityType.Tier2 -> "updated"
                    SecurityType.Tier3 -> "updated"
                }
            }

            is BrowserRequestResponse.Cancel -> "cancel"
        }

        val baseResponseObject = buildJsonObject {
            put("type", JsonPrimitive(type))
            put("status", JsonPrimitive(status))
        }

        val responseData = buildJsonObject {
            when (response) {
                is BrowserRequestResponse.PasswordRequestAccept -> {
                    val passT2Key = HkdfGenerator.generate(
                        inputKeyMaterial = sessionKey,
                        salt = hkdfSalt,
                        contextInfo = "PassT2",
                    )

                    val passwordEnc = encrypt(
                        key = passT2Key,
                        data = response.password,
                    )

                    put("passwordEnc", JsonPrimitive(passwordEnc.encodeBase64()))
                }

                is BrowserRequestResponse.DeleteLoginAccept -> Unit
                is BrowserRequestResponse.AddLoginAccept -> {
                    when (response.item.securityType) {
                        SecurityType.Tier1 -> Unit
                        SecurityType.Tier2,
                        SecurityType.Tier3,
                        -> {
                            val loginData = createLoginAcceptData(item = response.item, deviceId = deviceId, hkdfSalt = hkdfSalt, sessionKey = sessionKey)
                            put("login", loginData)
                        }
                    }
                }

                is BrowserRequestResponse.UpdateLoginAccept -> {
                    when (response.item.securityType) {
                        SecurityType.Tier1 -> Unit
                        SecurityType.Tier2,
                        SecurityType.Tier3,
                        -> {
                            val loginData = createLoginAcceptData(item = response.item, deviceId = deviceId, hkdfSalt = hkdfSalt, sessionKey = sessionKey)
                            put("login", loginData)
                        }
                    }
                }

                is BrowserRequestResponse.Cancel -> Unit
            }
        }

        return json.encodeToString(baseResponseObject + responseData)
    }

    private fun createLoginAcceptData(
        item: Item,
        deviceId: String,
        hkdfSalt: ByteArray,
        sessionKey: ByteArray,
    ): JsonElement {
        val password = (item.content as? ItemContent.Login)?.password.clearTextOrNull
        val passwordKey = HkdfGenerator.generate(
            inputKeyMaterial = sessionKey,
            salt = hkdfSalt,
            contextInfo = when (item.securityType) {
                SecurityType.Tier1 -> "PassT1"
                SecurityType.Tier2 -> "PassT2"
                SecurityType.Tier3 -> "PassT3"
            },
        )
        val passwordEnc = password?.let {
            SecretField.ClearText(
                encrypt(key = passwordKey, data = password).encodeBase64(),
            )
        }

        // TODO: BEv2
        val updatedItem = item.copy(
            content = (item.content as ItemContent.Login).copy(
                password = passwordEnc,
            ),
        )

        return json.encodeToJsonElement(
            itemMapper.mapItemContentLoginToJson(domain = updatedItem, deviceId = deviceId),
        )
    }

    private fun clearConnection() {
        expectedIncomingId = ""
        error = null
    }
}