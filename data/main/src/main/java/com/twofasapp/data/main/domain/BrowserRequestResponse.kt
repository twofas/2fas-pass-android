/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.Login

sealed interface BrowserRequestResponse {
    data class PasswordRequestAccept(
        val password: String,
    ) : BrowserRequestResponse

    data object DeleteLoginAccept : BrowserRequestResponse
    data class AddLoginAccept(val login: Login) : BrowserRequestResponse
    data class UpdateLoginAccept(val login: Login) : BrowserRequestResponse
    data object Cancel : BrowserRequestResponse
}