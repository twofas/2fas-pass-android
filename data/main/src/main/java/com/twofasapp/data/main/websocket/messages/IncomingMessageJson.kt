/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket.messages

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

@Serializable(with = IncomingMessageJson.Serializer::class)
internal sealed interface IncomingMessageJson {
    val scheme: Int
    val origin: String
    val originVersion: String
    val id: String?
    val action: String

    @Serializable
    data class Unknown(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
    ) : IncomingMessageJson

    @Serializable
    data class CloseWithError(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
        val payload: Payload,
    ) : IncomingMessageJson {
        @Serializable
        data class Payload(
            val errorCode: Int,
            val errorMessage: String,
        )
    }

    @Serializable
    data class Hello(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
        val payload: Payload,
    ) : IncomingMessageJson {
        @Serializable
        data class Payload(
            val browserName: String,
            val browserExtName: String,
            val browserVersion: String,
        )
    }

    @Serializable
    data class Challenge(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
        val payload: Payload,
    ) : IncomingMessageJson {
        @Serializable
        data class Payload(
            val hkdfSaltEnc: String,
        )
    }

    @Serializable
    data class InitTransferConfirmed(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
    ) : IncomingMessageJson

    @Serializable
    data class TransferChunkConfirmed(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
        val payload: Payload,
    ) : IncomingMessageJson {
        @Serializable
        data class Payload(
            val chunkIndex: Int,
        )
    }

    @Serializable
    data class TransferCompleted(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
    ) : IncomingMessageJson

    @Serializable
    data class PullRequest(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
        val payload: Payload,
    ) : IncomingMessageJson {
        @Serializable
        data class Payload(
            val dataEnc: String,
        )
    }

    @Serializable
    data class PullRequestCompleted(
        override val scheme: Int,
        override val origin: String,
        override val originVersion: String,
        override val id: String?,
        override val action: String,
    ) : IncomingMessageJson

    object Serializer : JsonContentPolymorphicSerializer<IncomingMessageJson>(IncomingMessageJson::class) {

        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<IncomingMessageJson> {
            return try {
                when (element.jsonObject["action"]?.jsonPrimitive?.content) {
                    "closeWithError" -> CloseWithError.serializer()
                    "hello" -> Hello.serializer()
                    "challenge" -> Challenge.serializer()
                    "initTransferConfirmed" -> InitTransferConfirmed.serializer()
                    "transferChunkConfirmed" -> TransferChunkConfirmed.serializer()
                    "transferCompleted" -> TransferCompleted.serializer()
                    "pullRequest" -> PullRequest.serializer()
                    "pullRequestCompleted" -> PullRequestCompleted.serializer()
                    else -> Unknown.serializer()
                }
            } catch (e: Exception) {
                Timber.e(e)
                Unknown.serializer()
            }
        }
    }
}