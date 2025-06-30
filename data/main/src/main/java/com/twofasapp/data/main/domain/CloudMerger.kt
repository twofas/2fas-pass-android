/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main.domain

import com.twofasapp.core.common.domain.DeletedItem

class CloudMerger {

    fun merge(local: VaultBackup, cloud: VaultBackup): CloudMerge {
        val localDeletedItems = local.deletedItems.orEmpty().toMutableList()
        val cloudDeletedItems = cloud.deletedItems.orEmpty().toMutableList()

        /**
         * Merging logins
         */
        val loginsMerge = mergeItems(
            localItems = local.logins.orEmpty(),
            localDeletedItems = localDeletedItems,
            cloudItems = cloud.logins.orEmpty(),
            cloudDeletedItems = cloudDeletedItems,
            getId = { it.id },
            getUpdatedAt = { it.updatedAt },
            markItemAsDeleted = { item, updatedAt, deletedAt ->
                item.copy(
                    updatedAt = updatedAt,
                    deleted = true,
                    deletedAt = deletedAt,
                )
            },
        )

        /**
         * Merging tags
         */
        val tagsMerge = mergeItems(
            localItems = local.tags.orEmpty(),
            localDeletedItems = localDeletedItems,
            cloudItems = cloud.tags.orEmpty(),
            cloudDeletedItems = cloudDeletedItems,
            getId = { it.id },
            getUpdatedAt = { it.updatedAt },
            markItemAsDeleted = { item, updatedAt, _ ->
                item.copy(
                    updatedAt = updatedAt,
                )
            },
        )

        val cloudMerge = CloudMerge(
            logins = loginsMerge,
            tags = tagsMerge,
            deletedItems = mutableListOf(),
        )

        /**
         * Merging deleted items
         */
        val addedLoginIds = loginsMerge.toAdd.map { it.id }
        val addedTagIds = tagsMerge.toAdd.map { it.id }

        val restoredItemIds = addedLoginIds + addedTagIds

        cloudMerge.deletedItems.apply {
            // Add all local and cloud deleted items
            addAll(localDeletedItems)
            addAll(cloudDeletedItems)
        }
            // Filter out deleted items that will be added to local items, as a result of that cloudMerge.
            // This is the case when cloud item is newer than local deleted item.
            .filter { restoredItemIds.contains(it.id).not() }
            // Sort by deletedAt in descending order to have the most recent deletions first
            .sortedByDescending { it.deletedAt }
            // Remove duplicates by id
            .distinctBy { it.id }

        return cloudMerge
    }

    private inline fun <T> mergeItems(
        localItems: List<T>,
        localDeletedItems: MutableList<DeletedItem>,
        cloudItems: List<T>,
        cloudDeletedItems: MutableList<DeletedItem>,
        getId: (T) -> String,
        getUpdatedAt: (T) -> Long,
        markItemAsDeleted: (T, Long, Long) -> T,
    ): CloudMerge.Result<T> {
        val result = CloudMerge.Result<T>()
        val cloudItemsMutable = cloudItems.toMutableList()

        localItems.forEach { localItem ->
            val localId = getId(localItem)
            val localUpdatedAt = getUpdatedAt(localItem)

            // Find local item in cloud
            val matchingCloudItem = cloudItemsMutable.find { getId(it) == localId }

            if (matchingCloudItem != null) {
                // If cloud item is newer -> update local item
                if (getUpdatedAt(matchingCloudItem) > localUpdatedAt) {
                    result.toUpdate.add(matchingCloudItem)
                }

                cloudItemsMutable.remove(matchingCloudItem)
            } else {
                // Local item not found in cloud, try to find it in deleted items
                val matchingCloudDeletedItem = cloudDeletedItems.find { it.id == localId }

                if (matchingCloudDeletedItem != null) {
                    if (matchingCloudDeletedItem.deletedAt > localUpdatedAt) {
                        // If deletion time is newer -> delete local item
                        val trashedItem = markItemAsDeleted(
                            localItem,
                            matchingCloudDeletedItem.deletedAt,
                            matchingCloudDeletedItem.deletedAt,
                        )
                        result.toDelete.add(trashedItem)
                    } else {
                        // If local item is newer than deletion time, it means it was restored locally
                        // and we should delete from deleted items
                        cloudDeletedItems.remove(matchingCloudDeletedItem)
                    }
                }
            }
        }

        // Iterate over cloud items which are not present in local and add them if they are not deleted
        cloudItemsMutable.forEach { cloudItem ->
            val cloudId = getId(cloudItem)
            val cloudUpdatedAt = getUpdatedAt(cloudItem)

            val matchingLocalDeletedItem = localDeletedItems.find { it.id == cloudId }

            if (matchingLocalDeletedItem != null) {
                if (cloudUpdatedAt > matchingLocalDeletedItem.deletedAt) {
                    // Item was deleted locally but in cloud it was updated -> add to local list
                    // and remove from deleted items
                    result.toAdd.add(cloudItem)
                    localDeletedItems.remove(matchingLocalDeletedItem)
                }
            } else {
                result.toAdd.add(cloudItem)
            }
        }

        return result
    }
}