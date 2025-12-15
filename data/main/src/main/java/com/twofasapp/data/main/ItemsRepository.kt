/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.data.main.domain.CloudMerge
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {
    fun observeItems(vaultId: String): Flow<List<ItemEncrypted>>
    suspend fun getItem(id: String): ItemEncrypted
    suspend fun getItems(): List<ItemEncrypted>
    suspend fun getItemsDecrypted(): List<Item>
    suspend fun getItemsDecryptedWithDeleted(): List<Item>
    suspend fun getItemsCount(): Int
    suspend fun decrypt(itemEncrypted: ItemEncrypted, decryptSecretFields: Boolean): Item?
    suspend fun decrypt(vaultCipher: VaultCipher, itemsEncrypted: List<ItemEncrypted>, decryptSecretFields: Boolean): List<Item>
    suspend fun saveItem(item: ItemEncrypted): String
    suspend fun saveItems(items: List<ItemEncrypted>)
    suspend fun importItems(items: List<Item>, triggerSync: Boolean = true)
    suspend fun getMostCommonUsernames(): List<String>
    suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Item>)
    suspend fun lockItems()
    suspend fun unlockItems()
    suspend fun updateTags(itemId: String, tagIds: List<String>)
    suspend fun updateItemsWithTags(map: Map<Item, Set<String>>)
    suspend fun deleteTag(tagId: String)
    suspend fun permanentlyDeleteAll()
}