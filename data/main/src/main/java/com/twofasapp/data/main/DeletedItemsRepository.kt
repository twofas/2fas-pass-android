/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.DeletedItem

interface DeletedItemsRepository {
    suspend fun getDeletedItems(vaultId: String): List<DeletedItem>
    suspend fun saveDeletedItems(entities: List<DeletedItem>)
    suspend fun clearDeletedItems(ids: List<String>)
    suspend fun clearAll(vaultId: String)
}