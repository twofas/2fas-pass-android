/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.cloudsync.ui.googledrive

internal data class GoogleDriveSyncUiState(
    val openedFromQuickSetup: Boolean = false,
    val startAuth: Boolean = false,
    val enabled: Boolean = false,
    val syncing: Boolean = false,
)