/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.s3

import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.services.common.BackupStorage
import com.twofasapp.data.cloud.services.common.CloudClient
import com.twofasapp.data.cloud.services.common.model.CloudIndexJson
import com.twofasapp.data.cloud.services.common.model.CloudIndexLockJson
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import java.net.URI
import java.time.Duration
import java.util.Date

internal class S3Client(
    appBuild: AppBuild,
    private val json: Json,
    private val timeProvider: TimeProvider,
    private val device: Device,
) : CloudClient(appBuild, json), BackupStorage<CloudConfig.S3> {

    override suspend fun testConnection(config: CloudConfig.S3) {
        // HEAD against the bucket root: a 404 unambiguously means the bucket is missing.
        executeRequest(
            config = config,
            method = "HEAD",
            objectKey = "",
            body = null,
            contentType = null,
        )
    }

    override suspend fun getIndex(config: CloudConfig.S3): CloudIndexJson {
        return try {
            val response = executeRequest(
                config = config,
                method = "GET",
                objectKey = IndexFilename,
                body = null,
                contentType = null,
            )
            val content = response.body<String>()
            runSafely { json.decodeFromString<CloudIndexJson>(content) }.getOrNull() ?: CloudIndexJson(emptyList())
        } catch (e: Exception) {
            if (e.isHttpNotFound()) {
                val emptyIndex = CloudIndexJson(emptyList())
                putIndex(config = config, index = emptyIndex)
                emptyIndex
            } else {
                e.printStackTrace()
                throw e
            }
        }
    }

    override suspend fun putIndex(config: CloudConfig.S3, index: CloudIndexJson) {
        val body = json.encodeToString(CloudIndexJson.serializer(), index).toByteArray(Charsets.UTF_8)
        executeRequest(
            config = config,
            method = "PUT",
            objectKey = IndexFilename,
            body = body,
            contentType = "application/json",
        )
    }

    override suspend fun obtainLock(config: CloudConfig.S3): Boolean {
        return try {
            val response = executeRequest(
                config = config,
                method = "GET",
                objectKey = IndexLockFilename,
                body = null,
                contentType = null,
            )
            val existingLockJson = response.body<String>()
            val existingLock = runSafely { json.decodeFromString<CloudIndexLockJson>(existingLockJson) }.getOrNull()

            if (existingLock == null ||
                existingLock.deviceId == device.uniqueId() ||
                timeProvider.currentTimeUtc() > existingLock.timestamp + Duration.ofSeconds(20).toMillis()
            ) {
                createLock(
                    config = config,
                    body = CloudIndexLockJson(
                        deviceId = device.uniqueId(),
                        timestamp = timeProvider.currentTimeUtc(),
                    ),
                )
            } else {
                false
            }
        } catch (e: Exception) {
            if (e.isHttpNotFound()) {
                createLock(
                    config = config,
                    body = CloudIndexLockJson(
                        deviceId = device.uniqueId(),
                        timestamp = timeProvider.currentTimeUtc(),
                    ),
                )
            } else {
                throw e
            }
        }
    }

    override suspend fun releaseLock(config: CloudConfig.S3) {
        runSafely {
            executeRequest(
                config = config,
                method = "DELETE",
                objectKey = IndexLockFilename,
                body = null,
                contentType = null,
            )
        }
    }

    private suspend fun createLock(config: CloudConfig.S3, body: CloudIndexLockJson): Boolean {
        val bytes = json.encodeToString(CloudIndexLockJson.serializer(), body).toByteArray(Charsets.UTF_8)
        executeRequest(
            config = config,
            method = "PUT",
            objectKey = IndexLockFilename,
            body = bytes,
            contentType = "application/json",
        )
        return true
    }

    override suspend fun getFile(config: CloudConfig.S3, filename: String): String? {
        return try {
            val response = executeRequest(
                config = config,
                method = "GET",
                objectKey = filename,
                body = null,
                contentType = null,
            )
            response.body<String>()
        } catch (e: Exception) {
            if (e.isHttpNotFound()) {
                null
            } else {
                throw e
            }
        }
    }

    override suspend fun putFile(config: CloudConfig.S3, filename: String, content: String) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        executeRequest(
            config = config,
            method = "PUT",
            objectKey = filename,
            body = bytes,
            contentType = "application/octet-stream",
        )
    }

    override suspend fun moveFile(config: CloudConfig.S3, source: String, destination: String) {
        // S3 has no native move: copy via x-amz-copy-source, then delete the source.
        val encodedBucket = S3SigV4Signer.percentEncodePathSegment(config.bucket)
        val encodedKey = S3SigV4Signer.percentEncodePathSegment(source)
        executeRequest(
            config = config,
            method = "PUT",
            objectKey = destination,
            body = null,
            contentType = null,
            extraHeaders = mapOf("x-amz-copy-source" to "/$encodedBucket/$encodedKey"),
        )

        executeRequest(
            config = config,
            method = "DELETE",
            objectKey = source,
            body = null,
            contentType = null,
        )
    }

    fun Exception.isHttpNotFound(): Boolean {
        return (this as? ClientRequestException)?.response?.status == HttpStatusCode.NotFound
    }

    private suspend fun executeRequest(
        config: CloudConfig.S3,
        method: String,
        objectKey: String,
        body: ByteArray?,
        contentType: String?,
        extraHeaders: Map<String, String> = emptyMap(),
    ): HttpResponse {
        val endpointUri = URI(config.endpoint)
        val endpointHost = endpointUri.host.orEmpty()
        val isVirtualHosted = endpointHost == config.bucket || endpointHost.startsWith("${config.bucket}.")

        val pathSegments = mutableListOf<String>()
        if (!isVirtualHosted && config.bucket.isNotEmpty()) {
            pathSegments.add(config.bucket)
        }
        if (objectKey.isNotEmpty()) {
            pathSegments.add(objectKey)
        }
        val encodedPath = "/" + pathSegments.joinToString("/") { S3SigV4Signer.percentEncodePathSegment(it) }

        val basePath = endpointUri.rawPath?.trimEnd('/').orEmpty()
        val fullPath = if (basePath.isEmpty()) encodedPath else basePath + encodedPath

        val scheme = endpointUri.scheme
        val port = if (endpointUri.port == -1) "" else ":${endpointUri.port}"
        val url = "$scheme://$endpointHost$port$fullPath"

        val bodySha256Hex = if (body != null && body.isNotEmpty()) {
            S3SigV4Signer.sha256Hex(body)
        } else {
            S3SigV4Signer.EMPTY_BODY_SHA256_HEX
        }

        val headersToSign = mutableMapOf<String, String>()
        if (contentType != null) headersToSign["Content-Type"] = contentType
        headersToSign.putAll(extraHeaders)

        val signed = S3SigV4Signer.sign(
            method = method,
            url = url,
            host = endpointHost,
            queryString = "",
            canonicalPath = fullPath,
            headersToSign = headersToSign,
            bodySha256Hex = bodySha256Hex,
            now = Date(timeProvider.currentTimeUtc()),
            config = config,
        )

        val client = if (config.allowUntrustedCertificate) untrustedHttpClient else httpClient
        return client.request(url) {
            this.method = HttpMethod.parse(method)
            signed.headers.forEach { (name, value) -> header(name, value) }
            if (body != null) {
                setBody(body)
            }
        }
    }
}