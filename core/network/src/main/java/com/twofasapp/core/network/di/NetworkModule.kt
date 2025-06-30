/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.network.di

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.di.KoinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

class NetworkModule : KoinModule {

    override fun provide() = module {
        singleOf(::KtorLogger)
        singleOf(::KtorLoggingInterceptor)

        single {
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
                coerceInputValues = true
            }
        }

        single {
            val isDebuggable = get<AppBuild>().debuggable

            HttpClient(OkHttp) {
                expectSuccess = true

                if (isDebuggable) {
                    install(get<KtorLoggingInterceptor>())
                }

                install(ContentNegotiation) {
                    json(get())
                }

                install(WebSockets)

                install(DefaultRequest) {
                    url("https://pass.2fas.com")
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }
}