/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.SecurityItem
import kotlinx.coroutines.flow.Flow

interface SecurityItemRepository {
    fun observeSecurityItem(vaultId: String): Flow<List<SecurityItem>>
    suspend fun getSecurityItems(vaultId: String): List<SecurityItem>
    fun observeSelectedSecurityItem(vaultId: String): Flow<SecurityItem?>
    suspend fun toggleSelectedSecurityItem(vaultId: String, securityItem: SecurityItem)
    suspend fun clearSelectedSecurityItem(vaultId: String)
}