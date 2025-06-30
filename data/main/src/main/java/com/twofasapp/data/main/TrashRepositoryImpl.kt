/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.domain.EncryptedLogin
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.local.LoginsLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.mapper.LoginMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class TrashRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val timeProvider: TimeProvider,
    private val loginsLocalSource: LoginsLocalSource,
    private val vaultsLocalSource: VaultsLocalSource,
    private val deletedItemsRepository: DeletedItemsRepository,
    private val cloudRepository: CloudRepository,
    private val loginMapper: LoginMapper,
) : TrashRepository {

    override fun observeDeleted(): Flow<List<EncryptedLogin>> {
        return loginsLocalSource.observeDeleted().map { list ->
            list.map { entity -> loginMapper.mapToDomain(entity) }
        }
    }

    override suspend fun trash(vararg id: String) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()
            val logins = loginsLocalSource.getLogins(id.toList()).map {
                it.copy(
                    deleted = true,
                    updatedAt = now,
                    deletedAt = now,
                )
            }

            loginsLocalSource.saveLogins(logins)

            deletedItemsRepository.saveDeletedItems(logins.map { it.let(loginMapper::mapToDeletedItem) })

            vaultsLocalSource.updateLastModificationTime(logins.first().vaultId, now)

            cloudRepository.sync()
        }
    }

    override suspend fun restore(vararg id: String) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()
            val logins = loginsLocalSource.getLoginsDeleted(id.toList()).map {
                it.copy(
                    deleted = false,
                    updatedAt = now,
                    deletedAt = null,
                )
            }

            loginsLocalSource.saveLogins(logins)

            deletedItemsRepository.clearDeletedItems(logins.map { it.id })

            vaultsLocalSource.updateLastModificationTime(logins.first().vaultId, now)

            cloudRepository.sync()
        }
    }

    override suspend fun delete(vararg id: String) {
        withContext(dispatchers.io) {
            val logins = loginsLocalSource.getLoginsDeleted(id.toList())
            loginsLocalSource.delete(id.toList())

            deletedItemsRepository.saveDeletedItems(logins.map { it.let(loginMapper::mapToDeletedItem) })
        }
    }
}