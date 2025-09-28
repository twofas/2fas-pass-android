/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.websocket.messages

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

@Serializable(with = BrowserRequestActionJson.Serializer::class)
internal sealed interface BrowserRequestActionJson {
    @SerialName("type")
    val type: String

    @Serializable
    data class Unknown(
        override val type: String,
    ) : BrowserRequestActionJson

    @Serializable
    data class SecretFieldRequest(
        override val type: String,
        @SerialName("data")
        val data: Data,
    ) : BrowserRequestActionJson {
        @Serializable
        data class Data(
            @ExperimentalSerializationApi
            @JsonNames("loginId", "itemId")
            val itemId: String,
        )
    }

    @Serializable
    data class DeleteItem(
        override val type: String,
        @SerialName("data")
        val data: Data,
    ) : BrowserRequestActionJson {
        @Serializable
        data class Data(
            @ExperimentalSerializationApi
            @JsonNames("loginId", "itemId")
            val itemId: String,
        )
    }

    @Serializable
    data class AddItem(
        override val type: String,
        @SerialName("data")
        val data: Data,
    ) : BrowserRequestActionJson {
        @Serializable
        data class Data(
            @SerialName("url")
            val url: String,
            @SerialName("username")
            val username: String?,
            @SerialName("passwordEnc")
            val passwordEnc: String?,
            @SerialName("usernamePasswordMobile")
            val usernamePasswordMobile: Boolean?,
        )
    }

    @Serializable
    data class UpdateItem(
        override val type: String,
        @SerialName("data")
        val data: Data,
    ) : BrowserRequestActionJson {
        @Serializable
        data class Data(
            @SerialName("id")
            val id: String,
            @SerialName("securityType")
            val securityType: Int,
            @SerialName("name")
            val name: String?,
            @SerialName("username")
            val username: String?,
            @SerialName("usernameMobile")
            val usernameMobile: Boolean?,
            @SerialName("passwordEnc")
            val passwordEnc: String?,
            @SerialName("passwordMobile")
            val passwordMobile: Boolean?,
            @SerialName("uris")
            val uris: List<Uri>?,
            @SerialName("notes")
            val notes: String?,
        ) {
            @Serializable
            data class Uri(
                @SerialName("text")
                val text: String,
                @SerialName("matcher")
                val matcher: Int,
            )
        }
    }

    object Serializer : JsonContentPolymorphicSerializer<BrowserRequestActionJson>(BrowserRequestActionJson::class) {

        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<BrowserRequestActionJson> {
            return try {
                when (element.jsonObject["type"]?.jsonPrimitive?.content) {
                    "sifRequest" -> SecretFieldRequest.serializer()
                    "deleteData" -> DeleteItem.serializer()
                    "addData" -> AddItem.serializer()
                    "updateData" -> UpdateItem.serializer()
                    else -> Unknown.serializer()
                }
            } catch (e: Exception) {
                Timber.e(e)
                Unknown.serializer()
            }
        }
    }
}