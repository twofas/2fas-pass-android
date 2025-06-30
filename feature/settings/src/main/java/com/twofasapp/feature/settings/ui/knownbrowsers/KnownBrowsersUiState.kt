/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.knownbrowsers

import com.twofasapp.data.main.domain.ConnectedBrowser

internal data class KnownBrowsersUiState(
    val connectedBrowsers: List<ConnectedBrowser> = emptyList(),
)