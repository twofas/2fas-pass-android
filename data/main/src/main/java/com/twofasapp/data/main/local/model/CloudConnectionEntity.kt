/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = CloudConnectionEntity.Serializer::class)
sealed interface CloudConnectionEntity {

    val type: String

    @Serializable
    data class GoogleDrive(
        @SerialName("type")
        override val type: String = TypeGoogleDrive,
        @SerialName("accountId")
        val accountId: String,
        @SerialName("credentialType")
        val credentialType: String,
    ) : CloudConnectionEntity

    @Serializable
    data class WebDav(
        @SerialName("type")
        override val type: String = TypeWebDav,
        @SerialName("url")
        val url: String,
        @SerialName("username")
        val username: String,
        @SerialName("password")
        val password: String,
        @SerialName("allowUntrustedCertificate")
        val allowUntrustedCertificate: Boolean,
    ) : CloudConnectionEntity

    @Serializable
    data class S3(
        @SerialName("type")
        override val type: String = TypeS3,
        @SerialName("endpoint")
        val endpoint: String,
        @SerialName("region")
        val region: String,
        @SerialName("bucket")
        val bucket: String,
        @SerialName("accessKeyId")
        val accessKeyId: String,
        @SerialName("secretAccessKey")
        val secretAccessKey: String,
        @SerialName("allowUntrustedCertificate")
        val allowUntrustedCertificate: Boolean,
    ) : CloudConnectionEntity

    companion object {
        const val TypeGoogleDrive = "GoogleDrive"
        const val TypeWebDav = "WebDav"
        const val TypeS3 = "S3"
    }

    object Serializer : JsonContentPolymorphicSerializer<CloudConnectionEntity>(CloudConnectionEntity::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CloudConnectionEntity> {
            return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
                TypeGoogleDrive -> GoogleDrive.serializer()
                TypeWebDav -> WebDav.serializer()
                TypeS3 -> S3.serializer()
                else -> throw IllegalArgumentException("Unknown CloudConnectionEntity type: ${element.jsonObject["type"]}")
            }
        }
    }
}