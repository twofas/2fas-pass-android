/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.webdav

internal data class WebDavSyncUiState(
    val configId: String? = null,
    val connecting: Boolean = false,
    val error: String? = null,
    val closeScreen: Boolean = false,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val allowUntrustedCertificate: Boolean = false,
) {
    val formValid: Boolean
        get() = url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
}