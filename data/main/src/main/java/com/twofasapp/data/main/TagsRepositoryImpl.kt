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
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.TagColor
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.domain.CloudMerge
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.local.ItemsLocalSource
import com.twofasapp.data.main.local.TagsLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.mapper.TagMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

internal class TagsRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val timeProvider: TimeProvider,
    private val tagsLocalSource: TagsLocalSource,
    private val itemsLocalSource: ItemsLocalSource,
    private val tagMapper: TagMapper,
    private val vaultCryptoScope: VaultCryptoScope,
    private val vaultsLocalSource: VaultsLocalSource,
    private val vaultsRepository: VaultsRepository,
    private val deletedItemsRepository: DeletedItemsRepository,
) : TagsRepository {

    private val selectedTag = MutableStateFlow<Map<String, Tag?>>(emptyMap())

    override fun observeTags(vaultId: String): Flow<List<Tag>> {
        return combine(
            tagsLocalSource.observe(vaultId).distinctUntilChanged(),
            itemsLocalSource.observe(vaultId).distinctUntilChanged(),
            { a, b -> Pair(a, b) },
        )
            .map { (tags, items) ->
                vaultCryptoScope.withVaultCipher(vaultId) {
                    tags.map { tag ->
                        tagMapper
                            .mapToDomain(entity = tag, vaultCipher = this)
                            .copy(assignedItemsCount = items.count {
                                it.tagIds.orEmpty().contains(tag.id)
                            })
                    }
                }
            }.catch {
                // Temporarily emit empty list when tags are being reencrypted
                emit(emptyList())
            }
    }

    override suspend fun getTags(vaultId: String): List<Tag> {
        return withContext(dispatchers.io) {
            val items = itemsLocalSource.getItems()

            tagsLocalSource.getTags(vaultId).let { tags ->
                vaultCryptoScope.withVaultCipher(vaultId) {
                    tags.map { tag ->
                        tagMapper
                            .mapToDomain(entity = tag, vaultCipher = this)
                            .copy(assignedItemsCount = items.count {
                                it.tagIds.orEmpty().contains(tag.id)
                            })
                    }
                }
            }
        }
    }

    override suspend fun saveTags(vararg tags: Tag) {
        withContext(dispatchers.io) {
            val tagsLastPosition = tagsLocalSource.getTags()
                .groupBy { it.vaultId }
                .mapValues { it.value.maxOf { tag -> tag.position } }

            val now = timeProvider.currentTimeUtc()
            val entities = tags
                .map { tag ->
                    tag.copy(
                        id = tag.id.ifBlank { Uuid.generate() },
                        position = if (tag.id.isBlank()) {
                            tagsLastPosition[tag.vaultId]?.plus(1) ?: 0
                        } else {
                            tag.position
                        },
                        updatedAt = now,
                    )
                }
                .groupBy { it.vaultId }
                .map { (vaultId, tags) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        tags.map { tag ->
                            tagMapper.mapToEntity(domain = tag, vaultCipher = this)
                        }
                    }
                }
                .flatten()

            tagsLocalSource.saveTags(entities)

            vaultsLocalSource.updateLastModificationTime(tags.first().vaultId, now)
        }
    }

    override suspend fun reencryptTags(vaultKeys: VaultKeys) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()
            val tags = getTags(vaultKeys.vaultId)

            vaultCryptoScope.withVaultCipher(vaultKeys) {
                val encryptedTags = tags.map { tag ->
                    tagMapper.mapToEntity(domain = tag, vaultCipher = this).copy(updatedAt = now)
                }

                tagsLocalSource.saveTags(encryptedTags)
            }

            vaultsLocalSource.updateLastModificationTime(vaultKeys.vaultId, now)
        }
    }

    override suspend fun deleteTags(vararg tags: Tag) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()

            tagsLocalSource.deleteTags(tags.map { it.id })

            tags.forEach { tag ->
                if (selectedTag.value.values.map { it?.id }.contains(tag.id)) {
                    clearSelectedTag(tag.vaultId)
                }
            }

            deletedItemsRepository.saveDeletedItems(
                entities = tags.map {
                    tagMapper.mapToDeletedItem(tag = it, deletedAt = now)
                },
            )

            vaultsLocalSource.updateLastModificationTime(tags.first().vaultId, now)
        }
    }

    override suspend fun importTags(tags: List<Tag>) {
        withContext(dispatchers.io) {
            val tagsLastPosition = tagsLocalSource.getTags()
                .groupBy { it.vaultId }
                .mapValues { it.value.maxOf { tag -> tag.position } }
            val now = timeProvider.currentTimeUtc()
            val vaultId = vaultsRepository.getVault().id
            val localTags = tagsLocalSource.getTags()
            val tagsToInsert = mutableListOf<Tag>()

            tags
                .forEach { newTag ->
                    val matchingTag = localTags.firstOrNull { it.id == newTag.id }

                    if (matchingTag != null) {
                        if (newTag.updatedAt > matchingTag.updatedAt) {
                            tagsToInsert.add(
                                newTag.copy(
                                    vaultId = matchingTag.vaultId,
                                    position = matchingTag.position,
                                ),
                            )
                        }
                    } else {
                        tagsToInsert.add(
                            newTag.copy(
                                id = newTag.id.ifBlank { Uuid.generate() },
                                vaultId = vaultId,
                                position = tagsLastPosition[vaultId]?.plus(1) ?: 0,
                                updatedAt = if (newTag.updatedAt == 0L) now else newTag.updatedAt,
                            ),
                        )
                    }
                }

            tagsToInsert
                .groupBy { it.vaultId }
                .map { (vaultId, tags) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        tags.map { tag ->
                            tagMapper.mapToEntity(domain = tag, vaultCipher = this)
                        }
                    }
                }
                .flatten()
                .also {
                    tagsLocalSource.saveTags(it)
                }
        }
    }

    override fun observeSelectedTag(vaultId: String): Flow<Tag?> {
        return combine(
            selectedTag.map { it[vaultId] },
            itemsLocalSource.observe(vaultId).distinctUntilChanged(),
            { a, b -> Pair(a, b) },
        ).map { (tag, items) ->
            tag?.copy(
                assignedItemsCount = items.count { it.tagIds.orEmpty().contains(tag.id) },
            )
        }
    }

    override suspend fun toggleSelectedTag(vaultId: String, tag: Tag) {
        selectedTag.update {
            if (it[vaultId] == tag) {
                emptyMap()
            } else {
                it.plus(vaultId to tag)
            }
        }
    }

    override suspend fun clearSelectedTag(vaultId: String) {
        selectedTag.update { it.minus(vaultId) }
    }

    override suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Tag>) {
        val vault = vaultsLocalSource.get().first()

        tagsLocalSource.saveTags(
            (cloudMerge.toAdd + cloudMerge.toUpdate).map { tag ->
                vaultCryptoScope.withVaultCipher(tag.vaultId) {
                    tagMapper.mapToEntity(domain = tag, vaultCipher = this)
                }
            },
        )

        tagsLocalSource.deleteTags(cloudMerge.toDelete.map { it.id })

        val mostRecentModificationTime = maxOf(
            itemsLocalSource.getMostRecentUpdatedAt(),
            tagsLocalSource.getMostRecentUpdatedAt(),
        )
        if (mostRecentModificationTime > vault.updatedAt) {
            vaultsLocalSource.updateLastModificationTime(vault.id, mostRecentModificationTime)
        }
    }

    override suspend fun observeSuggestedTagColor(vaultId: String): Flow<TagColor> {
        return observeTags(vaultId)
            .map { tags ->
                val colorHistogram = TagColor.values().associateWith { 0 } +
                        tags.map { tag -> tag.color ?: TagColor.default }
                            .groupingBy { it }
                            .eachCount()
                colorHistogram.entries.sortedBy { entry -> entry.value }.map { entry -> entry.key }
                    .firstOrNull() ?: TagColor.default
            }
    }

}