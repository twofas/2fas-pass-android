/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.cloudsync.ui.s3.S3SyncScreen
import com.twofasapp.feature.cloudsync.ui.webdav.WebDavSyncScreen

@Composable
fun WebDavSyncRoute(
    goBackToSync: () -> Unit,
) {
    WebDavSyncScreen(
        goBackToSync = goBackToSync,
    )
}

@Composable
fun S3SyncRoute(
    goBackToSync: () -> Unit,
) {
    S3SyncScreen(
        goBackToSync = goBackToSync,
    )
}