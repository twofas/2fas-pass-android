/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

sealed interface ConnectWebSocketResult {
    data object Success : ConnectWebSocketResult
    data class Failure(val errorCode: Int, val errorMessage: String) : ConnectWebSocketResult
}