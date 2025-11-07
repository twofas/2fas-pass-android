/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.items.Item

sealed interface BrowserRequestResponse {
    @Deprecated("Unsupported in BEv2")
    data class PasswordRequestAccept(
        val password: String,
    ) : BrowserRequestResponse

    @Deprecated("Unsupported in BEv2")
    data class AddLoginAccept(val item: Item) : BrowserRequestResponse

    @Deprecated("Unsupported in BEv2")
    data class UpdateLoginAccept(val item: Item) : BrowserRequestResponse

    data object FullSyncAccept : BrowserRequestResponse
    data class SecretFieldRequestAccept(
        val fields: Map<String, String>,
    ) : BrowserRequestResponse

    data object DeleteItemAccept : BrowserRequestResponse
    data class AddItemAccept(val item: Item) : BrowserRequestResponse
    data class UpdateItemAccept(val item: Item, val sifFetched: Boolean) : BrowserRequestResponse
    data object Cancel : BrowserRequestResponse
}