/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.domain

sealed interface CloudConfig {
    data class GoogleDrive(
        val id: String,
        val credentialType: String,
    ) : CloudConfig

    data class WebDav(
        val url: String,
        val username: String,
        val password: String,
        val allowUntrustedCertificate: Boolean,
    ) : CloudConfig
}