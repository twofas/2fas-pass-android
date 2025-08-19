/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.authenticate

import androidx.compose.runtime.Composable
import com.twofasapp.data.cloud.domain.CloudConfig

enum class CloudServiceType {
    GoogleDrive, LegacyGoogleDrive
}

val DefaultCloudServiceType = CloudServiceType.LegacyGoogleDrive

@Composable
fun AuthenticateCloudService(
    type: CloudServiceType,
    onDismissRequest: () -> Unit = {},
    onSuccess: (CloudConfig) -> Unit = {},
    onError: (Exception) -> Unit = {},
) {
    when (type) {
        CloudServiceType.GoogleDrive -> {
            AuthenticateWithGoogle(
                onDismissRequest = onDismissRequest,
                onSuccess = onSuccess,
                onError = onError,
            )
        }

        CloudServiceType.LegacyGoogleDrive -> {
            AuthenticateWithLegacyGoogle(
                onDismissRequest = onDismissRequest,
                onSuccess = onSuccess,
                onError = onError,
            )
        }
    }
}