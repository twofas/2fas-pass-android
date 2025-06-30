/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.requestmodal

import com.twofasapp.data.main.domain.BrowserRequestResponse
import com.twofasapp.data.main.domain.Identicon

internal data class RequestModalUiState(
    val notificationId: String? = null,
    val browserExtensionName: String? = null,
    val browserIdenticon: Identicon? = null,
    val requestState: RequestState? = null,
    val selectedResponse: BrowserRequestResponse? = null,
    val finishWithSuccess: Boolean = false,
)