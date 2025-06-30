/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

import android.annotation.SuppressLint
import java.lang.Exception

data class Uri(
    val scheme: String?,
    val host: String?,
    val path: String?,
    val query: String?,
    val queryParams: Map<String, String>,
) {
    companion object {
        @SuppressLint("UseKtx")
        fun from(text: String): Uri? {
            return try {
                val uriToParse = if (text.contains("://")) {
                    text
                } else {
                    "https://$text"
                }

                with(android.net.Uri.parse(uriToParse)) {
                    return Uri(
                        scheme = scheme,
                        host = host,
                        path = path,
                        query = query,
                        queryParams = queryParameterNames.associateWith { name ->
                            getQueryParameter(name).orEmpty()
                        },
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}