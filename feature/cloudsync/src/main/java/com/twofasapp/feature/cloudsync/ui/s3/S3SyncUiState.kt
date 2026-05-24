/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.s3

internal data class S3SyncUiState(
    val syncEnabled: Boolean = false,
    val syncing: Boolean = false,
    val endpoint: String = "",
    val region: String = "",
    val bucket: String = "",
    val accessKeyId: String = "",
    val secretAccessKey: String = "",
    val allowUntrustedCertificate: Boolean = false,
) {
    val formValid: Boolean
        get() = endpoint.isNotBlank() &&
            region.isNotBlank() &&
            bucket.isNotBlank() &&
            accessKeyId.isNotBlank() &&
            secretAccessKey.isNotBlank()
}