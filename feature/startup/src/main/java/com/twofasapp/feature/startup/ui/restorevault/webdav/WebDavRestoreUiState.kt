/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.webdav

internal data class WebDavRestoreUiState(
    val loading: Boolean = false,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val allowUntrustedCertificate: Boolean = false,
) {
    val formValid: Boolean
        get() = url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
}