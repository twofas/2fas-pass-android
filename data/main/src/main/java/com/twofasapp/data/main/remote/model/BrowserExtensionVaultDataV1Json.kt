/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.remote.model

import com.twofasapp.data.main.remote.model.vaultbackup.LoginJson
import kotlinx.serialization.Serializable

@Serializable
internal data class BrowserExtensionVaultDataV1Json(
    val logins: List<LoginJson>,
    val tags: List<TagJson>,
)