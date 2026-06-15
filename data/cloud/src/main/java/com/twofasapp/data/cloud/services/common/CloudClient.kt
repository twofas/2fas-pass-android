/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.services.common

import android.annotation.SuppressLint
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.logger.Flog
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal abstract class CloudClient(
    private val appBuild: AppBuild,
    private val json: Json,
) {
    companion object {
        const val IndexFilename = "index.2faspass"
        const val IndexLockFilename = "index.2faspass.lock"
    }

    protected val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true
            installLogging(appBuild)
            install(ContentNegotiation) { json(json) }
        }
    }

    protected val untrustedHttpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true

            engine {
                config {
                    val sslContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    hostnameVerifier { _, _ -> true }
                }
            }

            installLogging(appBuild)
            install(ContentNegotiation) { json(json) }
        }
    }

    private val trustAllCerts = arrayOf<TrustManager>(
        @SuppressLint("CustomX509TrustManager")
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        },
    )

    private fun HttpClientConfig<OkHttpConfig>.installLogging(appBuild: AppBuild) {
        if (appBuild.debuggable) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Flog.tag("Ktor").v(message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
}