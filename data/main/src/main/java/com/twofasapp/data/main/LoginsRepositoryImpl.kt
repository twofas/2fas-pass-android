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
import com.twofasapp.core.common.domain.EncryptedLogin
import com.twofasapp.core.common.domain.Login
import com.twofasapp.core.common.domain.LoginSecurityType
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.domain.CloudMerge
import com.twofasapp.data.main.local.LoginsLocalSource
import com.twofasapp.data.main.local.VaultsLocalSource
import com.twofasapp.data.main.local.model.CloudMergeEntity
import com.twofasapp.data.main.mapper.LoginEncryptionMapper
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
    private val loginsLocalSource: LoginsLocalSource,
    private val vaultsLocalSource: VaultsLocalSource,
    private val cloudRepository: CloudRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultMapper: VaultMapper,
    private val loginMapper: LoginMapper,
    private val loginEncryptionMapper: LoginEncryptionMapper,
    private val loginSecurityTypeMapper: LoginSecurityTypeMapper,
) : LoginsRepository {

    private val lockLogins = MutableStateFlow(false)

    override fun observeLogins(vaultId: String): Flow<List<EncryptedLogin>> {
        return combine(
            loginsLocalSource.observe(vaultId),
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
            loginsLocalSource.deleteAll()
        }
    }

    override suspend fun getLogin(id: String): EncryptedLogin {
        return withContext(dispatchers.io) {
            loginsLocalSource.getLogin(id).let(loginMapper::mapToDomain)
        }
    }

    override suspend fun getLogins(): List<EncryptedLogin> {
        return withContext(dispatchers.io) {
            loginsLocalSource.getLogins().map { loginMapper.mapToDomain(it) }
        }
    }

    override suspend fun getLoginsDecrypted(): List<Login> {
        return withContext(dispatchers.io) {
            getLogins()
                .groupBy { it.vaultId }
                .map { (vaultId, logins) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        logins.map {
                            loginEncryptionMapper.decryptLogin(it, this, decryptPassword = true)
                        }
                    }
                }
                .flatten()
                .filterNotNull()
        }
    }

    override suspend fun getLoginsDecryptedWithDeleted(): List<Login> {
        return withContext(dispatchers.io) {
            loginsLocalSource.getLoginsWithDeleted()
                .asSequence()
                .map { loginMapper.mapToDomain(it) }
                .groupBy { it.vaultId }
                .map { (vaultId, logins) ->
                    vaultCryptoScope.withVaultCipher(vaultId) {
                        logins.map {
                            loginEncryptionMapper.decryptLogin(it, this, decryptPassword = true)
                        }
                    }
                }
                .flatten()
                .filterNotNull()
                .toList()
        }
    }

    override suspend fun saveLogin(login: EncryptedLogin): String {
        return withContext(dispatchers.io) {
            val exists = login.id.isNotBlank()
            val now = timeProvider.currentTimeUtc()
            var loginId = ""

            if (exists) {
                loginId = login.id

                loginsLocalSource.saveLogin(
                    login.copy(
                        updatedAt = now,
                    ).let(loginMapper::mapToEntity),
                )
            } else {
                loginId = generateLoginUuid()

                loginsLocalSource.saveLogin(
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

    override suspend fun saveLogins(logins: List<EncryptedLogin>) {
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

            loginsLocalSource.saveLogins(entities)
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
                                loginEncryptionMapper.encryptLogin(login, this).let(loginMapper::mapToEntity)
                            }
                    }
                }
                .flatten()
                .also {
                    loginsLocalSource.saveLogins(it)

                    val mostRecentModificationTime = loginsLocalSource.getMostRecentUpdatedAt()
                    vaultsLocalSource.updateLastModificationTime(vault.id, mostRecentModificationTime)

                    if (triggerSync) {
                        cloudRepository.sync()
                    }
                }
        }
    }

    override suspend fun getLoginsCount(): Int {
        return withContext(dispatchers.io) {
            loginsLocalSource.countLogins()
        }
    }

    override suspend fun getMostCommonUsernames(): List<String> {
        return withContext(dispatchers.io) {
            // Take 6 most common usernames
            loginsLocalSource.getUsernamesFrequency()
                .mapNotNull { usernameFrequency ->
                    usernameFrequency.username?.let {
                        vaultCryptoScope.withVaultCipher(usernameFrequency.vaultId) {
                            when (usernameFrequency.securityType.let(loginSecurityTypeMapper::mapToDomainFromEntity)) {
                                LoginSecurityType.Tier1 -> decryptWithSecretKey(it)
                                LoginSecurityType.Tier2 -> decryptWithTrustedKey(it)
                                LoginSecurityType.Tier3 -> decryptWithTrustedKey(it)
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
                    loginsToAdd = cloudMerge.toAdd.map {
                        loginEncryptionMapper.encryptLogin(it, this).let(loginMapper::mapToEntity)
                    },
                    loginsToUpdate = cloudMerge.toUpdate.map {
                        loginEncryptionMapper.encryptLogin(it, this).let(loginMapper::mapToEntity)
                    },
                    loginsToTrash = cloudMerge.toDelete.map {
                        loginEncryptionMapper.encryptLogin(it, this).let(loginMapper::mapToEntity)
                    },
                )
            }

            loginsLocalSource.executeCloudMerge(cloudMergeEntity)

            val mostRecentModificationTime = loginsLocalSource.getMostRecentUpdatedAt()
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