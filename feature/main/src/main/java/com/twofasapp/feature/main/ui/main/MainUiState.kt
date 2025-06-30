/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import com.twofasapp.core.android.deeplinks.Deeplink
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.ConnectData

internal data class MainUiState(
    val cloudSyncError: Boolean = false,
    val events: List<MainUiEvent> = emptyList(),
)

internal sealed interface MainUiEvent {
    data class OpenDeeplink(val deeplink: Deeplink) : MainUiEvent
    data class ShowBrowserConnect(val browserConnectData: ConnectData) : MainUiEvent
    data class ShowBrowserRequest(val browserRequestData: BrowserRequestData) : MainUiEvent
}