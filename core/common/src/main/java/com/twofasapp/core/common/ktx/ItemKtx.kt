/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent

fun List<Item>.filterBySearchQuery(query: String): List<Item> {
    return filter { item ->
        item.content.name.contains(query, ignoreCase = true) ||
                when (item.content) {
                    is ItemContent.Unknown -> false

                    is ItemContent.Login -> {
                        item.content.username.orEmpty().contains(query, ignoreCase = true) ||
                                item.content.uris.any { it.text.contains(query, ignoreCase = true) }
                    }

                    is ItemContent.SecureNote -> false

                    is ItemContent.PaymentCard -> {
                        item.content.cardHolder.orEmpty().contains(query, ignoreCase = true) ||
                                item.content.cardNumberMask.orEmpty().contains(query, ignoreCase = true)
                    }
                }
    }.distinctBy { it.id }
}