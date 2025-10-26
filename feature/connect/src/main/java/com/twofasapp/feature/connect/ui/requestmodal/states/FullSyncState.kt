/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.requestmodal.states

internal data class FullSyncState(
    val onConfirmClick: () -> Unit = {},
    val onCancelClick: () -> Unit = {},
)