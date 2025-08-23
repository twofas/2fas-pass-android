/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.purchases.domain

data class Entitlements(
    val itemsLimit: Int,
    val unlimitedConnectedBrowsers: Boolean,
    val multiDeviceSync: Boolean,
) {
    companion object {
        val Free = Entitlements(
            itemsLimit = 200,
            unlimitedConnectedBrowsers = false,
            multiDeviceSync = false,
        )

        val Paid = Entitlements(
            itemsLimit = Int.MAX_VALUE,
            unlimitedConnectedBrowsers = true,
            multiDeviceSync = true,
        )
    }
}