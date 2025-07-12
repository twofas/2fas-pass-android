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
import com.twofasapp.core.common.domain.ItemEncrypted
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.domain.CloudMerge
import com.twofasapp.data.main.local.ItemsLocalSource
import com.twofasapp.data.main.local.LoginsLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.local.model.CloudMergeEntity
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.mapper.LoginMapper
import com.twofasapp.data.main.mapper.LoginSecurityTypeMapper
import com.twofasapp.data.main.mapper.VaultMapper
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class LoginsRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val timeProvider: TimeProvider,
    private val vaultCryptoScope: VaultCryptoScope,
    private val itemsLocalSource: ItemsLocalSource,
    private val loginsLocalSource: LoginsLocalSource,
    private val vaultsLocalSource: VaultsLocalSource,
    private val cloudRepository: CloudRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultMapper: VaultMapper,
    private val loginMapper: LoginMapper,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val loginSecurityTypeMapper: LoginSecurityTypeMapper,
) : LoginsRepository {

    private val lockLogins = MutableStateFlow(false)

    override fun observeLogins(vaultId: String): Flow<List<ItemEncrypted>> {
        return combine(
            itemsLocalSource.observe(vaultId),
            lockLogins,
            { a, b -> Pair(a, b) },
        )
            .filter { it.second.not() }
            .map { (list, _) ->
                list.map { loginMapper.mapToDomain(it) }
            }
            .flowOn(dispatchers.io)
    }

    override suspend fun permanentlyDeleteAll() {
        withContext(dispatchers.io) {
            itemsLocalSource.deleteAll()
        }
    }

    override suspend fun getLogin(id: String): ItemEncrypted {
        return withContext(dispatchers.io) {
            itemsLocalSource.getLogin(id).let(loginMapper::mapToDomain)
        }
    }

    override suspend fun getLogins(): List<ItemEncrypted> {
        return withContext(dispatchers.io) {
            itemsLocalSource.getLogins().map { loginMapper.mapToDomain(it) }
        }
    }

    override suspend fun getLoginsDecrypted(): List<Login> {
        return withContext(dispatchers.io) {
            getLogins()
                .groupBy { it.vaultId }
                .map { (vaultId, logins) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        logins.map {
                            itemEncryptionMapper.decryptLogin(it, this, decryptPassword = true)
                        }
                    }
                }
                .flatten()
                .filterNotNull()
        }
    }

    override suspend fun getLoginsDecryptedWithDeleted(): List<Login> {
        return withContext(dispatchers.io) {
            itemsLocalSource.getLoginsWithDeleted()
                .asSequence()
                .map { loginMapper.mapToDomain(it) }
                .groupBy { it.vaultId }
                .map { (vaultId, logins) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        logins.map {
                            itemEncryptionMapper.decryptLogin(it, this, decryptPassword = true)
                        }
                    }
                }
                .flatten()
                .filterNotNull()
                .toList()
        }
    }

    override suspend fun saveLogin(login: ItemEncrypted): String {
        return withContext(dispatchers.io) {
            val exists = login.id.isNotBlank()
            val now = timeProvider.currentTimeUtc()
            var loginId = ""

            if (exists) {
                loginId = login.id

                itemsLocalSource.saveLogin(
                    login.copy(
                        updatedAt = now,
                    ).let(loginMapper::mapToEntity),
                )
            } else {
                loginId = generateLoginUuid()

                itemsLocalSource.saveLogin(
                    login.copy(
                        id = loginId,
                        createdAt = now,
                        updatedAt = now,
                    ).let(loginMapper::mapToEntity),
                )
            }

            vaultsLocalSource.updateLastModificationTime(login.vaultId, now)

            cloudRepository.sync()

            loginId
        }
    }

    override suspend fun saveLogins(logins: List<ItemEncrypted>) {
        withContext(dispatchers.io) {
            if (logins.isEmpty()) return@withContext

            val now = timeProvider.currentTimeUtc()
            val entities = logins.map { login ->
                if (login.id.isNotBlank()) {
                    login.copy(
                        updatedAt = now,
                    ).let(loginMapper::mapToEntity)
                } else {
                    login.copy(
                        id = generateLoginUuid(),
                        createdAt = now,
                        updatedAt = now,
                    ).let(loginMapper::mapToEntity)
                }
            }

            itemsLocalSource.saveLogins(entities)
            vaultsLocalSource.updateLastModificationTime(entities.first().vaultId, now)

            cloudRepository.sync()
        }
    }

    override suspend fun lockLogins() {
        lockLogins.emit(true)
    }

    override suspend fun unlockLogins() {
        lockLogins.emit(false)
    }

    override suspend fun importLogins(logins: List<Login>, triggerSync: Boolean) {
        withContext(dispatchers.io) {
            val now = timeProvider.currentTimeUtc()
            val vault = vaultsLocalSource.get().first().let(vaultMapper::mapToDomain)
            val defaultSecurityType = settingsRepository.observeDefaultSecurityType().first()
            val localLogins = getLoginsDecrypted()
            val loginsToInsert = mutableListOf<Login>()

            logins
                .forEach { newLogin ->
                    val matchingLogin =
                        localLogins.firstOrNull { it.id == newLogin.id }
                            ?: localLogins.findContentEqualLogin(newLogin)

                    if (matchingLogin != null) {
                        if (newLogin.updatedAt > matchingLogin.updatedAt) {
                            loginsToInsert.add(newLogin)
                        }
                    } else {
                        loginsToInsert.add(
                            newLogin.copy(
                                id = newLogin.id.ifBlank { generateLoginUuid() },
                                vaultId = vault.id,
                                securityType = if (newLogin.id.isBlank()) {
                                    defaultSecurityType
                                } else {
                                    newLogin.securityType
                                },
                                createdAt = if (newLogin.createdAt == 0L) now else newLogin.createdAt,
                                updatedAt = if (newLogin.updatedAt == 0L) now else newLogin.updatedAt,
                            ),
                        )
                    }
                }

            loginsToInsert
                .groupBy { it.vaultId }
                .map { (vaultId, logins) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        logins
                            .map { login ->
                                itemEncryptionMapper.encryptLogin(login, this)
                                    .let(loginMapper::mapToEntity)
                            }
                    }
                }
                .flatten()
                .also {
                    itemsLocalSource.saveLogins(it)

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

    override suspend fun getLoginsCount(): Int {
        return withContext(dispatchers.io) {
            itemsLocalSource.countLogins()
        }
    }

    override suspend fun decrypt(itemEncrypted: ItemEncrypted, decryptPassword: Boolean): Login? {
        return withContext(dispatchers.io) {
            vaultCryptoScope.withVaultCipher(itemEncrypted.vaultId) {
                itemEncryptionMapper.decryptLogin(
                    itemEncrypted = itemEncrypted,
                    vaultCipher = this,
                    decryptPassword = decryptPassword,
                )
            }
        }
    }

    override suspend fun decrypt(vaultCipher: VaultCipher, itemsEncrypted: List<ItemEncrypted>, decryptPassword: Boolean): List<Login> {
        return withContext(dispatchers.io) {
            itemsEncrypted.mapNotNull { itemEncrypted ->
                itemEncryptionMapper.decryptLogin(
                    itemEncrypted = itemEncrypted,
                    vaultCipher = vaultCipher,
                    decryptPassword = decryptPassword,
                )
            }
        }
    }

    override suspend fun getMostCommonUsernames(): List<String> {
        return withContext(dispatchers.io) {
            // Take 6 most common usernames
            itemsLocalSource.getUsernamesFrequency()
                .mapNotNull { usernameFrequency ->
                    usernameFrequency.username?.let {
                        vaultCryptoScope.withVaultCipher(usernameFrequency.vaultId) {
                            when (usernameFrequency.securityType.let(loginSecurityTypeMapper::mapToDomainFromEntity)) {
                                SecurityType.Tier1 -> decryptWithSecretKey(it)
                                SecurityType.Tier2 -> decryptWithTrustedKey(it)
                                SecurityType.Tier3 -> decryptWithTrustedKey(it)
                            }
                        }
                    }
                }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
        }
    }

    override suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Login>) {
        withContext(dispatchers.io) {
            Timber.d("Execute cloud merge: $cloudMerge")

            val vault = vaultsLocalSource.get().first().let(vaultMapper::mapToDomain)

            val cloudMergeEntity = vaultCryptoScope.withVaultCipher(vault.id) {
                CloudMergeEntity(
                    loginsToAdd = listOf(),
                    loginsToUpdate = listOf(),
                    loginsToTrash = listOf(),

//                    loginsToAdd = cloudMerge.toAdd.map {
//                        itemEncryptionMapper.encryptLogin(it, this).let(loginMapper::mapToEntity)
//                    },
//                    loginsToUpdate = cloudMerge.toUpdate.map {
//                        itemEncryptionMapper.encryptLogin(it, this).let(loginMapper::mapToEntity)
//                    },
//                    loginsToTrash = cloudMerge.toDelete.map {
//                        itemEncryptionMapper.encryptLogin(it, this).let(loginMapper::mapToEntity)
//                    },
                )
            }

            itemsLocalSource.executeCloudMerge(cloudMergeEntity)

            val mostRecentModificationTime = itemsLocalSource.getMostRecentUpdatedAt()
            if (mostRecentModificationTime > vault.updatedAt) {
                vaultsLocalSource.updateLastModificationTime(vault.id, mostRecentModificationTime)
            }
        }
    }

    private fun List<Login>.findContentEqualLogin(login: Login): Login? {
        forEach { entry ->
            if (entry.isContentEqual(login)) {
                return entry
            }
        }

        return null
    }

    private fun generateLoginUuid(): String {
        return Uuid.generate()
    }
}