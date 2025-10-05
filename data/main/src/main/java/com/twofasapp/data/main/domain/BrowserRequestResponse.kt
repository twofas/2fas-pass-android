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
    data class PasswordRequestAccept(
        val password: String,
    ) : BrowserRequestResponse

    data object DeleteItemAccept : BrowserRequestResponse
    data class AddLoginAccept(val item: Item) : BrowserRequestResponse // TODO: BEv2
    data class UpdateLoginAccept(val item: Item) : BrowserRequestResponse // TODO: BEv2
    data object Cancel : BrowserRequestResponse
}