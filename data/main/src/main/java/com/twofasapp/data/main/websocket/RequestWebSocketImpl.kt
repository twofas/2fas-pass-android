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
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.decodeString
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeByteArray
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.ktx.sha256
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.BackupRepository
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.TagsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.BrowserRequestAction
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.BrowserRequestResponse
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.RequestWebSocketResult
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.ItemMapper
import com.twofasapp.data.main.mapper.ItemSecurityTypeMapper
import com.twofasapp.data.main.mapper.PaymentCardValidator
import com.twofasapp.data.main.mapper.TagMapper
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
    override val itemEncryptionMapper: ItemEncryptionMapper,
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val vaultsRepository: VaultsRepository,
    private val dispatchers: Dispatchers,
    private val androidKeyStore: AndroidKeyStore,
    private val json: Json,
    private val browserRequestsRemoteSource: BrowserRequestsRemoteSource,
    private val notificationManager: NotificationManager,
    private val itemMapper: ItemMapper,
    private val itemSecurityTypeMapper: ItemSecurityTypeMapper,
    private val uriMatcherMapper: UriMatcherMapper,
    private val tagsRepository: TagsRepository,
    private val tagMapper: TagMapper,
) : RequestWebSocket, WebSocketDelegate {

    override var version: Int = ConnectData.CurrentSchema
    override var expectedIncomingId: String = ""
    private var error: Exception? = null
    private val chunks = mutableListOf<String>()
    private val chunkSize = 2 * 1024 * 1024

    override suspend fun open(
        requestData: BrowserRequestData,
        onBrowserRequestAction: suspend (BrowserRequestAction) -> BrowserRequestResponse,
    ): RequestWebSocketResult {
        try {
            withContext(dispatchers.io) {
                Timber.d("Request: $requestData")

                version = requestData.version
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

                                        val requestString = request.decodeString()

                                        Timber.d("Request data: $requestString")

                                        val action = json.decodeFromString<BrowserRequestActionJson>(requestString)
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
                                        publicKey = requestData.pkPersBe,
                                        sessionId = newSessionId,
                                    )

                                    sendMessage(
                                        createOutgoingMessage(
                                            payload = OutgoingPayloadJson.CloseWithSuccess,
                                        ),
                                    )
                                }
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
        Timber.d("Request action: $this")

        return when (this) {
            is BrowserRequestActionJson.PasswordRequest -> {
                BrowserRequestAction.PasswordRequest(
                    type = type,
                    item = getItem(data.itemId) ?: throw WebSocketException(1501, "Could not find requested item."),
                )
            }

            is BrowserRequestActionJson.AddLogin -> {
                val newItem = Item.create(
                    contentType = ItemContentType.Login,
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

            is BrowserRequestActionJson.FullSync -> {
                BrowserRequestAction.FullSync(
                    type = type,
                )
            }

            is BrowserRequestActionJson.SecretFieldRequest -> {
                BrowserRequestAction.SecretFieldRequest(
                    type = type,
                    item = getItem(data.itemId) ?: throw WebSocketException(1501, "Could not find requested item."),
                )
            }

            is BrowserRequestActionJson.DeleteItem -> {
                BrowserRequestAction.DeleteItem(
                    type = type,
                    item = getItem(data.itemId) ?: throw WebSocketException(1501, "Could not find requested item."),
                )
            }

            is BrowserRequestActionJson.AddItem -> {
                val contentType = when (data.contentType) {
                    "login" -> ItemContentType.Login
                    "secureNote" -> ItemContentType.SecureNote
                    "paymentCard" -> ItemContentType.PaymentCard
                    else -> throw IllegalArgumentException("Unsupported item type")
                }

                val newItemKey = HkdfGenerator.generate(
                    inputKeyMaterial = sessionKey,
                    salt = hkdfSalt,
                    contextInfo = "ItemNew",
                )

                val content = when (contentType) {
                    is ItemContentType.Unknown -> throw IllegalArgumentException("Unsupported item type")
                    is ItemContentType.Login -> {
                        ItemContent.Login.Empty.copy(
                            name = (data.content.url.orEmpty().toUri().host ?: data.content.url).orEmpty().removePrefix("www."),
                            uris = listOf(ItemUri(text = data.content.url.orEmpty())),
                            username = when (data.content.username?.action) {
                                "generate" -> itemsRepository.getMostCommonUsernames().firstOrNull().orEmpty()
                                else -> data.content.username?.value.orEmpty()
                            },
                            password = when (data.content.s_password?.action) {
                                "generate" -> {
                                    SecretField.ClearText(
                                        PasswordGenerator.generatePassword(
                                            settingsRepository.observePasswordGeneratorSettings().first(),
                                        ),
                                    )
                                }

                                else -> {
                                    data.content.s_password?.let { encryptedPassword ->
                                        if (encryptedPassword.value.isEmpty()) {
                                            SecretField.ClearText("")
                                        } else {
                                            val password = decrypt(
                                                key = newItemKey,
                                                data = EncryptedBytes(encryptedPassword.value.decodeBase64()),
                                            )

                                            SecretField.ClearText(password.decodeString())
                                        }
                                    }
                                }
                            },
                        )
                    }

                    is ItemContentType.SecureNote -> {
                        ItemContent.SecureNote.Empty.copy(
                            name = data.content.name.orEmpty(),
                            text = data.content.s_text?.let { encryptedText ->
                                if (encryptedText.isEmpty()) {
                                    SecretField.ClearText("")
                                } else {
                                    val text = decrypt(
                                        key = newItemKey,
                                        data = EncryptedBytes(encryptedText.decodeBase64()),
                                    )

                                    SecretField.ClearText(text.decodeString())
                                }
                            },
                        )
                    }

                    is ItemContentType.PaymentCard -> {
                        val cardNumber = data.content.s_cardNumber?.let { encryptedCardNumber ->
                            if (encryptedCardNumber.isEmpty()) {
                                SecretField.ClearText("")
                            } else {
                                val cardNumber = decrypt(
                                    key = newItemKey,
                                    data = EncryptedBytes(encryptedCardNumber.decodeBase64()),
                                )

                                SecretField.ClearText(cardNumber.decodeString().replace(" ", ""))
                            }
                        }

                        ItemContent.PaymentCard.Empty.copy(
                            name = data.content.name.orEmpty(),
                            cardHolder = data.content.cardHolder,
                            cardNumber = cardNumber,
                            cardNumberMask = cardNumber?.value?.replace(" ", "")?.takeLast(4),
                            cardIssuer = PaymentCardValidator.detectCardIssuer(cardNumber?.value),
                            expirationDate = data.content.s_expirationDate?.let { encryptedExpirationDate ->
                                if (encryptedExpirationDate.isEmpty()) {
                                    SecretField.ClearText("")
                                } else {
                                    val expirationDate = decrypt(
                                        key = newItemKey,
                                        data = EncryptedBytes(encryptedExpirationDate.decodeBase64()),
                                    )

                                    SecretField.ClearText(expirationDate.decodeString())
                                }
                            },
                            securityCode = data.content.s_securityCode?.let { encryptedSecurityCode ->
                                if (encryptedSecurityCode.isEmpty()) {
                                    SecretField.ClearText("")
                                } else {
                                    val securityCode = decrypt(
                                        key = newItemKey,
                                        data = EncryptedBytes(encryptedSecurityCode.decodeBase64()),
                                    )

                                    SecretField.ClearText(securityCode.decodeString())
                                }
                            },
                        )
                    }
                }

                BrowserRequestAction.AddItem(
                    type = type,
                    item = Item.create(
                        contentType = contentType,
                        content = content,
                    ),
                )
            }

            is BrowserRequestActionJson.UpdateItem -> {
                val item = getItem(data.itemId)
                    ?: throw WebSocketException(1501, "Could not find requested item.")

                val contentType = when (data.contentType) {
                    "login" -> ItemContentType.Login
                    "secureNote" -> ItemContentType.SecureNote
                    "paymentCard" -> ItemContentType.PaymentCard
                    else -> throw IllegalArgumentException("Unsupported item type")
                }

                val securityType = data.securityType?.let(itemSecurityTypeMapper::mapToDomainFromJson) ?: item.securityType
                val tagIds = data.tags ?: item.tagIds

                val updateItemKey = HkdfGenerator.generate(
                    inputKeyMaterial = sessionKey,
                    salt = hkdfSalt,
                    contextInfo = when (item.securityType) {
                        SecurityType.Tier1 -> "ItemT1"
                        SecurityType.Tier2 -> "ItemT2"
                        SecurityType.Tier3 -> "ItemT3"
                    },
                )

                val updatedContent = when (contentType) {
                    is ItemContentType.Unknown -> throw IllegalArgumentException("Unsupported item type")
                    is ItemContentType.Login -> {
                        val existingContent = item.content as ItemContent.Login
                        existingContent.copy(
                            name = data.content.name ?: existingContent.name,
                            username = when (data.content.username?.action) {
                                "generate" -> itemsRepository.getMostCommonUsernames().firstOrNull().orEmpty()
                                else -> data.content.username?.value ?: existingContent.username
                            },
                            password = when (data.content.s_password?.action) {
                                "generate" -> {
                                    SecretField.ClearText(
                                        PasswordGenerator.generatePassword(
                                            settingsRepository.observePasswordGeneratorSettings().first(),
                                        ),
                                    )
                                }

                                else -> {
                                    data.content.s_password?.let { encryptedPassword ->
                                        if (encryptedPassword.value.isEmpty()) {
                                            SecretField.ClearText("")
                                        } else {
                                            val password = decrypt(
                                                key = updateItemKey,
                                                data = EncryptedBytes(encryptedPassword.value.decodeBase64()),
                                            )

                                            SecretField.ClearText(password.decodeString())
                                        }
                                    } ?: existingContent.password
                                }
                            },
                            notes = data.content.notes ?: existingContent.notes,
                            uris = data.content.uris?.map { uri ->
                                ItemUri(
                                    text = uri.text,
                                    matcher = uriMatcherMapper.mapToDomainFromJson(uri.matcher),
                                )
                            } ?: existingContent.uris,
                        )
                    }

                    is ItemContentType.SecureNote -> {
                        val existingContent = item.content as ItemContent.SecureNote
                        existingContent.copy(
                            name = data.content.name ?: existingContent.name,
                            text = data.content.s_text?.let { encryptedText ->
                                if (encryptedText.isEmpty()) {
                                    SecretField.ClearText("")
                                } else {
                                    val text = decrypt(
                                        key = updateItemKey,
                                        data = EncryptedBytes(encryptedText.decodeBase64()),
                                    )

                                    SecretField.ClearText(text.decodeString())
                                }
                            } ?: existingContent.text,
                        )
                    }

                    is ItemContentType.PaymentCard -> {
                        val existingContent = item.content as ItemContent.PaymentCard
                        val cardNumber = data.content.s_cardNumber?.let { encryptedCardNumber ->
                            if (encryptedCardNumber.isEmpty()) {
                                SecretField.ClearText("")
                            } else {
                                val cardNumber = decrypt(
                                    key = updateItemKey,
                                    data = EncryptedBytes(encryptedCardNumber.decodeBase64()),
                                )

                                SecretField.ClearText(cardNumber.decodeString().replace(" ", ""))
                            }
                        } ?: existingContent.cardNumber

                        existingContent.copy(
                            name = data.content.name ?: existingContent.name,
                            cardHolder = data.content.cardHolder ?: existingContent.cardHolder,
                            cardNumber = cardNumber,
                            expirationDate = data.content.s_expirationDate?.let { encryptedExpirationDate ->
                                if (encryptedExpirationDate.isEmpty()) {
                                    SecretField.ClearText("")
                                } else {
                                    val expirationDate = decrypt(
                                        key = updateItemKey,
                                        data = EncryptedBytes(encryptedExpirationDate.decodeBase64()),
                                    )

                                    SecretField.ClearText(expirationDate.decodeString())
                                }
                            } ?: existingContent.expirationDate,
                            cardIssuer = cardNumber?.clearTextOrNull?.let { PaymentCardValidator.detectCardIssuer(it) } ?: existingContent.cardIssuer,
                            securityCode = data.content.s_securityCode?.let { encryptedSecurityCode ->
                                if (encryptedSecurityCode.isEmpty()) {
                                    SecretField.ClearText("")
                                } else {
                                    val securityCode = decrypt(
                                        key = updateItemKey,
                                        data = EncryptedBytes(encryptedSecurityCode.decodeBase64()),
                                    )

                                    SecretField.ClearText(securityCode.decodeString())
                                }
                            } ?: existingContent.securityCode,
                            notes = data.content.notes ?: existingContent.notes,
                        )
                    }
                }

                BrowserRequestAction.UpdateItem(
                    type = type,
                    sifFetched = data.sifFetched,
                    item = item,
                    updatedItem = item.copy(
                        securityType = securityType,
                        content = updatedContent,
                        tagIds = tagIds,
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

            is BrowserRequestResponse.SecretFieldRequestAccept -> "accept"
            is BrowserRequestResponse.DeleteItemAccept -> "accept"
            is BrowserRequestResponse.FullSyncAccept -> "accept"
            is BrowserRequestResponse.AddItemAccept -> {
                when (response.item.securityType) {
                    SecurityType.Tier1 -> "addedInT1"
                    SecurityType.Tier2 -> "added"
                    SecurityType.Tier3 -> "added"
                }
            }

            is BrowserRequestResponse.UpdateItemAccept -> {
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

                is BrowserRequestResponse.SecretFieldRequestAccept -> {
                    val itemT2Key = HkdfGenerator.generate(
                        inputKeyMaterial = sessionKey,
                        salt = hkdfSalt,
                        contextInfo = "ItemT2",
                    )

                    put("expireInSeconds", JsonPrimitive(180))
                    put(
                        key = "data",
                        element = buildJsonObject {
                            response.fields.forEach { field ->
                                val fieldEnc = encrypt(
                                    key = itemT2Key,
                                    data = field.value,
                                )

                                put(
                                    key = field.key,
                                    element = JsonPrimitive(fieldEnc.encodeBase64()),
                                )
                            }
                        },
                    )
                }

                is BrowserRequestResponse.FullSyncAccept -> {
                    val dataKey = HkdfGenerator.generate(
                        inputKeyMaterial = sessionKey,
                        salt = hkdfSalt,
                        contextInfo = "Data",
                    )

                    val itemT3Key = HkdfGenerator.generate(
                        inputKeyMaterial = sessionKey,
                        salt = hkdfSalt,
                        contextInfo = "ItemT3",
                    )

                    val vaultDataGzip = backupRepository.createSerializedVaultDataForBrowserExtension(
                        version = 2,
                        vaultId = vaultsRepository.getVault().id,
                        deviceId = device.uniqueId(),
                        encryptionKey = itemT3Key,
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
                    val totalSize = vaultDataGzip.size

                    put("totalChunks", JsonPrimitive(totalChunks))
                    put("totalSize", JsonPrimitive(totalSize))
                    put("sha256GzipVaultDataEnc", JsonPrimitive(sha256EncGzipVaultData.encodeBase64()))
                }

                is BrowserRequestResponse.DeleteItemAccept -> Unit

                is BrowserRequestResponse.AddItemAccept -> {
                    when (response.item.securityType) {
                        SecurityType.Tier1 -> Unit
                        SecurityType.Tier2,
                        SecurityType.Tier3,
                        -> {
                            val itemData = createItemAcceptData(
                                item = response.item,
                                includeSecretFields = true,
                                hkdfSalt = hkdfSalt,
                                sessionKey = sessionKey,
                            )

                            val tagsData = createTagsData(
                                vaultId = response.item.vaultId,
                            )

                            put("data", itemData)
                            put("tags", tagsData)
                        }
                    }
                }

                is BrowserRequestResponse.UpdateItemAccept -> {
                    when (response.item.securityType) {
                        SecurityType.Tier1 -> Unit
                        SecurityType.Tier2,
                        SecurityType.Tier3,
                        -> {
                            val itemData = createItemAcceptData(
                                item = response.item,
                                includeSecretFields = response.sifFetched,
                                hkdfSalt = hkdfSalt,
                                sessionKey = sessionKey,
                            )

                            val tagsData = createTagsData(
                                vaultId = response.item.vaultId,
                            )

                            put("data", itemData)
                            put("tags", tagsData)
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

        val updatedItem = item.copy(
            content = (item.content as ItemContent.Login).copy(
                password = passwordEnc,
            ),
        )

        return json.encodeToJsonElement(
            itemMapper.mapToJsonV1(domain = updatedItem, deviceId = deviceId),
        )
    }

    private fun createItemAcceptData(
        item: Item,
        includeSecretFields: Boolean,
        hkdfSalt: ByteArray,
        sessionKey: ByteArray,
    ): JsonElement {
        val secretFieldKey = HkdfGenerator.generate(
            inputKeyMaterial = sessionKey,
            salt = hkdfSalt,
            contextInfo = when (item.securityType) {
                SecurityType.Tier1 -> "ItemT1"
                SecurityType.Tier2 -> "ItemT2"
                SecurityType.Tier3 -> "ItemT3"
            },
        )

        val contentWithEncryptedFields = itemEncryptionMapper.encryptSecretFields(
            content = item.content,
            encryptionKey = secretFieldKey,
        )

        val updatedItem = item.copy(
            vaultId = item.vaultId,
            content = when (contentWithEncryptedFields) {
                is ItemContent.Login -> {
                    contentWithEncryptedFields.copy(
                        password = if (includeSecretFields) {
                            (contentWithEncryptedFields.password as? SecretField.Encrypted)?.let { encryptedField ->
                                SecretField.ClearText(encryptedField.value.encodeBase64())
                            }
                        } else {
                            null
                        },
                    )
                }

                is ItemContent.SecureNote -> {
                    contentWithEncryptedFields.copy(
                        text = if (includeSecretFields) {
                            (contentWithEncryptedFields.text as? SecretField.Encrypted)?.let { encryptedField ->
                                SecretField.ClearText(encryptedField.value.encodeBase64())
                            }
                        } else {
                            null
                        },
                    )
                }

                is ItemContent.PaymentCard -> {
                    contentWithEncryptedFields.copy(
                        cardNumber = if (includeSecretFields) {
                            (contentWithEncryptedFields.cardNumber as? SecretField.Encrypted)?.let { encryptedField ->
                                SecretField.ClearText(encryptedField.value.encodeBase64())
                            }
                        } else {
                            null
                        },
                        expirationDate = if (includeSecretFields) {
                            (contentWithEncryptedFields.expirationDate as? SecretField.Encrypted)?.let { encryptedField ->
                                SecretField.ClearText(encryptedField.value.encodeBase64())
                            }
                        } else {
                            null
                        },
                        securityCode = if (includeSecretFields) {
                            (contentWithEncryptedFields.securityCode as? SecretField.Encrypted)?.let { encryptedField ->
                                SecretField.ClearText(encryptedField.value.encodeBase64())
                            }
                        } else {
                            null
                        },
                    )
                }

                is ItemContent.Unknown -> contentWithEncryptedFields
            },
        )

        return json.encodeToJsonElement(
            itemMapper.mapToJson(updatedItem),
        )
    }

    private suspend fun createTagsData(vaultId: String): JsonElement {
        val tags = tagsRepository.getTags(vaultId)

        return json.encodeToJsonElement(
            tagMapper.mapToJson(tags),
        )
    }

    private fun clearConnection() {
        expectedIncomingId = ""
        error = null
    }
}