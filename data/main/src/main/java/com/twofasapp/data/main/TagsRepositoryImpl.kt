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
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.domain.CloudMerge
import com.twofasapp.data.main.domain.Tag
import com.twofasapp.data.main.domain.VaultKeys
import com.twofasapp.data.main.local.TagsLocalSource
import com.twofasapp.data.main.mapper.TagMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class TagsRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val timeProvider: TimeProvider,
    private val localSource: TagsLocalSource,
    private val tagMapper: TagMapper,
    private val vaultCryptoScope: VaultCryptoScope,
    private val vaultsRepository: VaultsRepository,
    private val deletedItemsRepository: DeletedItemsRepository,
) : TagsRepository {

    override fun observeTags(vaultId: String): Flow<List<Tag>> {
        return localSource.observe(vaultId).map { tags ->
            vaultCryptoScope.withVaultCipher(vaultId) {
                tags.map { tag ->
                    tagMapper.mapToDomain(entity = tag, vaultCipher = this)
                }.sortedBy { it.position }
            }
        }.catch { emit(emptyList()) } // TODO: Handle when tags added (this is due to master password change)
    }

    override suspend fun getTags(vaultId: String): List<Tag> {
        return withContext(dispatchers.io) {
            localSource.getTags(vaultId).let { tags ->
                vaultCryptoScope.withVaultCipher(vaultId) {
                    tags.map { tag ->
                        tagMapper.mapToDomain(entity = tag, vaultCipher = this)
                    }.sortedBy { it.position }
                }
            }
        }
    }

    override suspend fun saveTags(tags: List<Tag>) {
        withContext(dispatchers.io) {
            val tagsLastPosition = localSource.getTags()
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

            localSource.saveTags(entities)
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

                localSource.saveTags(encryptedTags)
            }
        }
    }

    override suspend fun deleteTags(tags: List<Tag>) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()

            localSource.deleteTags(tags.map { it.id })

            deletedItemsRepository.saveDeletedItems(
                entities = tags.map {
                    tagMapper.mapToDeletedItem(tag = it, deletedAt = now)
                },
            )
        }
    }

    override suspend fun importTags(tags: List<Tag>) {
        withContext(dispatchers.io) {
            val tagsLastPosition = localSource.getTags()
                .groupBy { it.vaultId }
                .mapValues { it.value.maxOf { tag -> tag.position } }
            val now = timeProvider.currentTimeUtc()
            val vaultId = vaultsRepository.getVault().id
            val localTags = localSource.getTags()
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
                    localSource.saveTags(it)
                }
        }
    }

    override suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Tag>) {
        localSource.saveTags(
            (cloudMerge.toAdd + cloudMerge.toUpdate).map { tag ->
                vaultCryptoScope.withVaultCipher(tag.vaultId) {
                    tagMapper.mapToEntity(domain = tag, vaultCipher = this)
                }
            },
        )

        localSource.deleteTags(cloudMerge.toDelete.map { it.id })
    }
}