/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.webdav

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
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import java.time.Duration

internal class WebDavClient(
    appBuild: AppBuild,
    private val json: Json,
    private val timeProvider: TimeProvider,
    private val device: Device,
) : CloudClient(appBuild, json), BackupStorage<CloudConfig.WebDav> {

    override suspend fun getIndex(config: CloudConfig.WebDav): CloudIndexJson {
        return try {
            // Get index, decode it if exists
            val content = getHttpClient(config).get("${config.url}/$IndexFilename") {
                basicAuth(config.username, config.password)
            }.body<String>()

            // If parsing failed -> return empty index
            runSafely { json.decodeFromString<CloudIndexJson>(content) }.getOrNull() ?: CloudIndexJson(emptyList())
        } catch (e: Exception) {
            if (e.isHttpNotFound()) {
                // Index does not exists -> create one
                val emptyIndex = CloudIndexJson(emptyList())

                // Make sure path exists
                runSafely {
                    getHttpClient(config).request(config.url) {
                        basicAuth(config.username, config.password)
                        method = HttpMethod("MKCOL")
                    }
                }

                // Create empty index
                putIndex(
                    config = config,
                    index = emptyIndex,
                )

                emptyIndex
            } else {
                e.printStackTrace()
                throw e
            }
        }
    }

    override suspend fun putIndex(config: CloudConfig.WebDav, index: CloudIndexJson) {
        getHttpClient(config).put("${config.url}/$IndexFilename") {
            basicAuth(config.username, config.password)
            contentType(ContentType.Application.Json)
            setBody(index)
        }
    }

    override suspend fun obtainLock(config: CloudConfig.WebDav): Boolean {
        return try {
            // Try to get lock
            val existingLockJson = getHttpClient(config).get("${config.url}/$IndexLockFilename") {
                basicAuth(config.username, config.password)
            }.body<String>()

            val existingLock = runSafely { json.decodeFromString<CloudIndexLockJson>(existingLockJson) }.getOrNull()

            if (
                existingLock == null ||
                existingLock.deviceId == device.uniqueId() ||
                timeProvider.currentTimeUtc() > existingLock.timestamp + Duration.ofSeconds(20).toMillis()
            ) {
                // Create new lock if it does not exists or is outdated or is coming from the same device
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

    override suspend fun releaseLock(config: CloudConfig.WebDav) {
        runSafely {
            getHttpClient(config).delete("${config.url}/$IndexLockFilename") {
                basicAuth(config.username, config.password)
            }
        }
    }

    private suspend fun createLock(config: CloudConfig.WebDav, body: CloudIndexLockJson): Boolean {
        return try {
            getHttpClient(config).put("${config.url}/$IndexLockFilename") {
                basicAuth(config.username, config.password)
                contentType(ContentType.Application.Json)
                setBody(body)
                header("Overwrite", "T")
            }

            true
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getFile(config: CloudConfig.WebDav, filename: String): String? {
        return try {
            getHttpClient(config).get("${config.url}/$filename") {
                basicAuth(config.username, config.password)
            }.body<String>()
        } catch (e: Exception) {
            if (e.isHttpNotFound()) {
                // File does not exists, we will create one
                return null
            } else {
                throw e
            }
        }
    }

    override suspend fun putFile(config: CloudConfig.WebDav, filename: String, content: String) {
        getHttpClient(config).put("${config.url}/$filename") {
            basicAuth(config.username, config.password)
            contentType(ContentType.Application.Json)
            setBody(content)
            header("Overwrite", "T")
        }
    }

    override suspend fun moveFile(config: CloudConfig.WebDav, source: String, destination: String) {
        val destinationFullPath = "${config.url}/$destination"

        getHttpClient(config).request("${config.url}/$source") {
            method = HttpMethod("MOVE")
            basicAuth(config.username, config.password)
            header("Destination", destinationFullPath)
            header("Overwrite", "T")
        }
    }

    suspend fun wipeOut(config: CloudConfig.WebDav) {
        getHttpClient(config).delete("${config.url}/") {
            basicAuth(config.username, config.password)
        }
    }

    private fun Exception.isHttpNotFound(): Boolean {
        return (this as? ClientRequestException)?.response?.status == HttpStatusCode.NotFound
    }

    private fun getHttpClient(config: CloudConfig.WebDav) =
        if (config.allowUntrustedCertificate) {
            untrustedHttpClient
        } else {
            httpClient
        }
}