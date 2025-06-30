/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.local.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface CloudConfigEntity {
    @Serializable
    data class GoogleDrive(
        val id: String,
        val credentialType: String,
    ) : CloudConfigEntity

    @Serializable
    data class WebDav(
        val username: String,
        val password: String,
        val url: String,
        val allowUntrustedCertificate: Boolean,
    ) : CloudConfigEntity
}