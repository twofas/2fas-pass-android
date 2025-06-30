/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createdecryptionkit

import com.twofasapp.feature.decryptionkit.generator.DecryptionKit

internal data class CreateDecryptionKitUiState(
    val decryptionKit: DecryptionKit = DecryptionKit.Empty,
)