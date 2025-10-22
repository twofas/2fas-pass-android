/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.remote.model

import kotlinx.serialization.Serializable

@Serializable
internal data class BrowserExtensionVaultDataV2Json(
    val items: List<ItemJson>,
    val tags: List<TagJson>,
)