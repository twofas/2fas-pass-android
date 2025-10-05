/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.items.Item

sealed interface BrowserRequestAction {
    val type: String

    @Deprecated("Used in V1. Can be removed later.")
    data class PasswordRequest(
        override val type: String,
        val item: Item,
    ) : BrowserRequestAction

    @Deprecated("Used in V1. Can be removed later.")
    data class AddLogin(
        override val type: String,
        val item: Item,
    ) : BrowserRequestAction

    @Deprecated("Used in V1. Can be removed later.")
    data class UpdateLogin(
        override val type: String,
        val item: Item,
        val updatedItem: Item,
    ) : BrowserRequestAction

    data class SecretFieldRequest(
        override val type: String,
        val item: Item,
    ) : BrowserRequestAction

    data class DeleteItem(
        override val type: String,
        val item: Item,
    ) : BrowserRequestAction
}