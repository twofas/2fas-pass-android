/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.lazy

abstract class ListItem(key: Any? = null, type: Any? = null) {
    val key: Any = key ?: javaClass
    val type: Any = type ?: javaClass
}