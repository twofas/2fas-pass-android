/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.items.ItemEncrypted
import kotlinx.coroutines.flow.Flow

interface TrashRepository {
    fun observeDeleted(): Flow<List<ItemEncrypted>>
    suspend fun trash(vararg id: String)
    suspend fun restore(vararg id: String)
    suspend fun delete(vararg id: String)
}