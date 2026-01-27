/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.SecurityItem
import com.twofasapp.data.main.local.ItemsLocalSource
import com.twofasapp.data.main.mapper.SecurityItemMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class SecurityItemRepositoryImpl(
    private val itemsLocalSource: ItemsLocalSource,
    private val securityItemMapper: SecurityItemMapper
) : SecurityItemRepository {

    private val selectedSecurityItems = MutableStateFlow<Map<String, SecurityItem?>>(emptyMap())

    override fun observeSecurityItem(vaultId: String): Flow<List<SecurityItem>> {
        return itemsLocalSource.observe(vaultId).distinctUntilChanged()
            .map { items -> securityItemMapper.mapToDomain(items) }
    }

    override suspend fun getSecurityItems(vaultId: String): List<SecurityItem> {
        val items = itemsLocalSource.getItems()
        return securityItemMapper.mapToDomain(items)
    }

    override fun observeSelectedSecurityItem(vaultId: String): Flow<SecurityItem?> {
        return selectedSecurityItems.map { it[vaultId] }
    }

    override suspend fun toggleSelectedSecurityItem(
        vaultId: String,
        securityItem: SecurityItem
    ) {
        selectedSecurityItems.update {
            if (it[vaultId]?.type == securityItem.type) {
                it.minus(vaultId)
            } else {
                it.plus(vaultId to securityItem)
            }
        }
    }

    override suspend fun clearSelectedSecurityItem(vaultId: String) {
        selectedSecurityItems.update { it.minus(vaultId) }
    }
}