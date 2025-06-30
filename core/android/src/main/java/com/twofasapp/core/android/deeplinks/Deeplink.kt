/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.deeplinks

import com.twofasapp.core.android.navigation.Screen

sealed interface Deeplink {

    /**
     * Deeplink to a single screen or multiple screens stack
     */
    data class ToScreen(val screens: List<Screen>) : Deeplink {
        constructor(
            screen: Screen,
        ) : this(listOf(screen))
    }
}