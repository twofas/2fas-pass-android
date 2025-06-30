/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.navigation

inline fun <reified T : Screen> screenRoute(): String {
    return T::class.java.kotlin.qualifiedName.orEmpty()
}