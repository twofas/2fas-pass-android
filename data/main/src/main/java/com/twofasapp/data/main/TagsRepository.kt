/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.main.domain.CloudMerge
import com.twofasapp.data.main.domain.Tag
import com.twofasapp.data.main.domain.VaultKeys
import kotlinx.coroutines.flow.Flow

interface TagsRepository {
    fun observeTags(vaultId: String): Flow<List<Tag>>
    suspend fun getTags(vaultId: String): List<Tag>
    suspend fun saveTags(tags: List<Tag>)
    suspend fun deleteTags(tags: List<Tag>)
    suspend fun importTags(tags: List<Tag>)
    suspend fun reencryptTags(vaultKeys: VaultKeys)
    suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Tag>)
}