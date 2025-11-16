/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemEncrypted
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.domain.CloudMerge
import com.twofasapp.data.main.local.ItemsLocalSource
import com.twofasapp.data.main.local.TagsLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.local.model.CloudMergeEntity
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.ItemMapper
import com.twofasapp.data.main.mapper.VaultMapper
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class ItemsRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val timeProvider: TimeProvider,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemsLocalSource: ItemsLocalSource,
    private val vaultsLocalSource: VaultsLocalSource,
    private val tagsLocalSource: TagsLocalSource,
    private val cloudRepository: CloudRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultMapper: VaultMapper,
    private val itemMapper: ItemMapper,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ItemsRepository {

    private val lockObservability = MutableStateFlow(false)

    override fun observeItems(vaultId: String): Flow<List<ItemEncrypted>> {
        return combine(
            itemsLocalSource.observe(vaultId),
            lockObservability,
            { a, b -> Pair(a, b) },
        )
            .filter { it.second.not() }
            .map { (list, _) ->
                list.map { itemMapper.mapToDomain(it) }
            }
            .flowOn(dispatchers.io)
    }

    override suspend fun permanentlyDeleteAll() {
        withContext(dispatchers.io) {
            itemsLocalSource.deleteAll()
        }
    }

    override suspend fun getItem(id: String): ItemEncrypted {
        return withContext(dispatchers.io) {
            itemsLocalSource.getItem(id).let(itemMapper::mapToDomain)
        }
    }

    override suspend fun getItems(): List<ItemEncrypted> {
        return withContext(dispatchers.io) {
            itemsLocalSource.getItems().map { itemMapper.mapToDomain(it) }
        }
    }

    override suspend fun getItemsDecrypted(): List<Item> {
        return withContext(dispatchers.io) {
            getItems()
                .groupBy { it.vaultId }
                .map { (vaultId, items) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        items.map {
                            itemEncryptionMapper.decryptItem(it, this, decryptSecretFields = true)
                        }
                    }
                }
                .flatten()
                .filterNotNull()
        }
    }

    override suspend fun getItemsDecryptedWithDeleted(): List<Item> {
        return withContext(dispatchers.io) {
            itemsLocalSource.getItemsWithDeleted()
                .asSequence()
                .map { itemMapper.mapToDomain(it) }
                .groupBy { it.vaultId }
                .map { (vaultId, items) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        items.map {
                            itemEncryptionMapper.decryptItem(it, this, decryptSecretFields = true)
                        }
                    }
                }
                .flatten()
                .filterNotNull()
                .toList()
        }
    }

    override suspend fun saveItem(item: ItemEncrypted): String {
        return withContext(dispatchers.io) {
            val exists = item.id.isNotBlank()
            val now = timeProvider.currentTimeUtc()
            var itemId = ""

            if (exists) {
                itemId = item.id

                itemsLocalSource.saveItem(
                    item.copy(
                        updatedAt = now,
                    ).let(itemMapper::mapToEntity),
                )
            } else {
                itemId = generateItemUuid()

                itemsLocalSource.saveItem(
                    item.copy(
                        id = itemId,
                        createdAt = now,
                        updatedAt = now,
                    ).let(itemMapper::mapToEntity),
                )
            }

            vaultsLocalSource.updateLastModificationTime(item.vaultId, now)

            cloudRepository.sync()

            itemId
        }
    }

    override suspend fun saveItems(items: List<ItemEncrypted>) {
        withContext(dispatchers.io) {
            if (items.isEmpty()) return@withContext

            val now = timeProvider.currentTimeUtc()
            val entities = items.map { login ->
                if (login.id.isNotBlank()) {
                    login.copy(
                        updatedAt = now,
                    ).let(itemMapper::mapToEntity)
                } else {
                    login.copy(
                        id = generateItemUuid(),
                        createdAt = now,
                        updatedAt = now,
                    ).let(itemMapper::mapToEntity)
                }
            }

            itemsLocalSource.saveItems(entities)
            vaultsLocalSource.updateLastModificationTime(entities.first().vaultId, now)

            cloudRepository.sync()
        }
    }

    override suspend fun lockItems() {
        lockObservability.emit(true)
    }

    override suspend fun unlockItems() {
        lockObservability.emit(false)
    }

    override suspend fun updateTags(tags: List<String>, vararg ids: String) {
        val now = timeProvider.currentTimeUtc()
        val items = itemsLocalSource.getItems(ids.toList())
        val updatedItems = items.map { item ->
            item.copy(
                tagIds = tags,
                updatedAt = now,
            )
        }

        itemsLocalSource.saveItems(updatedItems)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun deleteTag(tagId: String) {
        GlobalScope.launch(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()
            val items = itemsLocalSource.getItemsWithDeleted()
            val itemsToUpdate = items
                .filter { item ->
                    item.tagIds.orEmpty().map { it.lowercase() }.contains(tagId.lowercase())
                }
                .map { item ->
                    item.copy(
                        tagIds = item.tagIds.orEmpty().map { it.lowercase() }.minus(tagId.lowercase()),
                        updatedAt = now,
                    )
                }

            itemsLocalSource.saveItems(itemsToUpdate)
        }
    }

    override suspend fun importItems(items: List<Item>, triggerSync: Boolean) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()
            val vault = vaultsLocalSource.get().first().let(vaultMapper::mapToDomain)
            val defaultSecurityType = settingsRepository.observeDefaultSecurityType().first()
            val localItems = getItemsDecrypted()
            val itemsToInsert = mutableListOf<Item>()

            items
                .forEach { newItem ->
                    val matchingItem =
                        localItems.firstOrNull { it.id == newItem.id }

                    if (matchingItem != null) {
                        if (newItem.updatedAt > matchingItem.updatedAt) {
                            itemsToInsert.add(
                                newItem.copy(
                                    vaultId = vault.id,
                                ),
                            )
                        }
                    } else {
                        itemsToInsert.add(
                            newItem.copy(
                                id = newItem.id.ifBlank { generateItemUuid() },
                                vaultId = vault.id,
                                securityType = if (newItem.id.isBlank()) {
                                    defaultSecurityType
                                } else {
                                    newItem.securityType
                                },
                                createdAt = if (newItem.createdAt == 0L) now else newItem.createdAt,
                                updatedAt = if (newItem.updatedAt == 0L) now else newItem.updatedAt,
                            ),
                        )
                    }
                }

            vaultCryptoScope.withVaultCipher(vault.id) {
                itemsToInsert
                    .map { item ->
                        itemEncryptionMapper.encryptItem(item, this)
                            .let(itemMapper::mapToEntity)
                    }
            }.also { itemsEncrypted ->
                itemsLocalSource.saveItems(itemsEncrypted)

                val mostRecentModificationTime = itemsLocalSource.getMostRecentUpdatedAt()
                vaultsLocalSource.updateLastModificationTime(
                    vault.id,
                    mostRecentModificationTime,
                )

                if (triggerSync) {
                    cloudRepository.sync()
                }
            }
        }
    }

    override suspend fun getItemsCount(): Int {
        return withContext(dispatchers.io) {
            itemsLocalSource.countItems()
        }
    }

    override suspend fun decrypt(itemEncrypted: ItemEncrypted, decryptSecretFields: Boolean): Item? {
        return withContext(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(itemEncrypted.vaultId) {
                itemEncryptionMapper.decryptItem(
                    itemEncrypted = itemEncrypted,
                    vaultCipher = this,
                    decryptSecretFields = decryptSecretFields,
                )
            }
        }
    }

    override suspend fun decrypt(vaultCipher: VaultCipher, itemsEncrypted: List<ItemEncrypted>, decryptSecretFields: Boolean): List<Item> {
        return withContext(dispatchers.io) {
            itemsEncrypted.mapNotNull { itemEncrypted ->
                itemEncryptionMapper.decryptItem(
                    itemEncrypted = itemEncrypted,
                    vaultCipher = vaultCipher,
                    decryptSecretFields = decryptSecretFields,
                )
            }
        }
    }

    override suspend fun getMostCommonUsernames(): List<String> {
        return withContext(dispatchers.io) {
            getItemsDecrypted()
                .map { it.content }
                .filterIsInstance<ItemContent.Login>()
                .mapNotNull { it.username }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(8)
                .map { it.key }
        }
    }

    override suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Item>) {
        withContext(dispatchers.io) {
            Timber.d("Execute cloud merge: $cloudMerge")

            val vault = vaultsLocalSource.get().first().let(vaultMapper::mapToDomain)

            val cloudMergeEntity = vaultCryptoScope.withVaultCipher(vault.id) {
                CloudMergeEntity(
                    itemsToAdd = cloudMerge.toAdd.map {
                        itemEncryptionMapper.encryptItem(it, this).let(itemMapper::mapToEntity)
                    },
                    itemsToUpdate = cloudMerge.toUpdate.map {
                        itemEncryptionMapper.encryptItem(it, this).let(itemMapper::mapToEntity)
                    },
                    itemsToTrash = cloudMerge.toDelete.map {
                        itemEncryptionMapper.encryptItem(it, this).let(itemMapper::mapToEntity)
                    },
                )
            }

            itemsLocalSource.executeCloudMerge(cloudMergeEntity)

            val mostRecentModificationTime = maxOf(
                itemsLocalSource.getMostRecentUpdatedAt(),
                tagsLocalSource.getMostRecentUpdatedAt(),
            )
            if (mostRecentModificationTime > vault.updatedAt) {
                vaultsLocalSource.updateLastModificationTime(vault.id, mostRecentModificationTime)
            }
        }
    }

    private fun generateItemUuid(): String {
        return Uuid.generate()
    }
}