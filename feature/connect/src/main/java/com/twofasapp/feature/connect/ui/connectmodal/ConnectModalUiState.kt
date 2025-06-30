/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.connectmodal

import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.Identicon

internal data class ConnectModalUiState(
    val browserExtensionName: String? = null,
    val browserIdenticon: Identicon? = null,
    val connectData: ConnectData? = null,
    val connectState: ConnectState? = null,
    val finishWithSuccess: Boolean = false,
)