/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.domain

data class Vault(
    val id: String,
    val name: String,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    companion object {
        val Empty = Vault(
            id = "",
            name = "",
            createdAt = 0L,
            updatedAt = 0L,
        )
    }
}