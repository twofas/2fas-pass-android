/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

data class Tag(
    val id: String,
    val vaultId: String,
    val name: String,
    val color: String?,
    val position: Int,
    val updatedAt: Long,
)