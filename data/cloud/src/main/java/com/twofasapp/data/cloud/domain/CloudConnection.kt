/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.domain

sealed interface CloudConnection {

    data class GoogleDrive(
        val accountId: String,
        val credentialType: String,
    ) : CloudConnection

    data class WebDav(
        val url: String,
        val username: String,
        val password: String,
        val allowUntrustedCertificate: Boolean,
    ) : CloudConnection

    data class S3(
        val endpoint: String,
        val region: String,
        val bucket: String,
        val accessKeyId: String,
        val secretAccessKey: String,
        val allowUntrustedCertificate: Boolean,
    ) : CloudConnection
}