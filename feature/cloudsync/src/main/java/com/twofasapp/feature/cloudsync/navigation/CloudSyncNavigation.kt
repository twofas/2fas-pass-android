/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.navigation

import androidx.compose.runtime.Composable
import com.twofasapp.feature.cloudsync.ui.googledrive.GoogleDriveSyncScreen
import com.twofasapp.feature.cloudsync.ui.webdav.WebDavSyncScreen

@Composable
fun GoogleDriveSyncRoute(
    goBackToSync: () -> Unit,
    goBackToSettings: () -> Unit,
) {
    GoogleDriveSyncScreen(
        goBackToSync = goBackToSync,
        goBackToSettings = goBackToSettings,
    )
}

@Composable
fun WebDavSyncRoute(
    goBackToSync: () -> Unit,
    goBackToSettings: () -> Unit,
) {
    WebDavSyncScreen(
        goBackToSync = goBackToSync,
        goBackToSettings = goBackToSettings,
    )
}