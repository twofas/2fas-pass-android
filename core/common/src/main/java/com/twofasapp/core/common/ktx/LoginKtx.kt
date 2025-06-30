/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

import com.twofasapp.core.common.domain.Login

fun List<Login>.filterBySearchQuery(query: String): List<Login> {
    return filter { login ->
        login.name.contains(query, ignoreCase = true) ||
            login.username.orEmpty().contains(query, ignoreCase = true) ||
            login.uris.any { it.text.contains(query, ignoreCase = true) }
    }.distinctBy { it.id }
}