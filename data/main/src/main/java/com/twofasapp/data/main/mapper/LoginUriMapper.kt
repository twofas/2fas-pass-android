/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.EncryptedLoginUri
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.data.main.remote.model.LoginUriJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class LoginUriMapper(
    private val uriMatcherMapper: LoginUriMatcherMapper,
) {

    fun mapToEntity(domain: EncryptedLoginUri): String {
        return with(domain) {
            buildJsonObject {
                put("text", JsonPrimitive(text.bytes.encodeBase64()))
                put("matcher", JsonPrimitive(uriMatcherMapper.mapToEntity(matcher)))
            }.toString()
        }
    }

    fun mapToDomain(entity: String): EncryptedLoginUri {
        val jsonObject = Json.parseToJsonElement(entity).jsonObject
        return EncryptedLoginUri(
            text = EncryptedBytes(jsonObject["text"]?.jsonPrimitive?.content.orEmpty().decodeBase64()),
            matcher = jsonObject["matcher"]?.jsonPrimitive?.content?.toInt().let { uriMatcherMapper.mapToDomainFromEntity(it) },
        )
    }

    fun mapToJson(domain: LoginUri): LoginUriJson {
        return with(domain) {
            LoginUriJson(
                text = text,
                matcher = uriMatcherMapper.mapToJson(matcher),
            )
        }
    }

    fun mapToDomain(json: LoginUriJson): LoginUri {
        return with(json) {
            LoginUri(
                text = text,
                matcher = matcher.let { uriMatcherMapper.mapToDomainFromJson(it) },
            )
        }
    }
}