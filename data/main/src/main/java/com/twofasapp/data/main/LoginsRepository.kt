/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.ItemEncrypted
import com.twofasapp.core.common.domain.Login
import com.twofasapp.data.main.domain.CloudMerge
import kotlinx.coroutines.flow.Flow

interface LoginsRepository {
    fun observeLogins(vaultId: String): Flow<List<ItemEncrypted>>
    suspend fun getLogin(id: String): ItemEncrypted
    suspend fun getLogins(): List<ItemEncrypted>
    suspend fun getLoginsDecrypted(): List<Login>
    suspend fun getLoginsDecryptedWithDeleted(): List<Login>
    suspend fun getLoginsCount(): Int
    suspend fun decrypt(itemEncrypted: ItemEncrypted, decryptPassword: Boolean): Login?
    suspend fun decrypt(vaultCipher: VaultCipher, itemsEncrypted: List<ItemEncrypted>, decryptPassword: Boolean): List<Login>
    suspend fun saveLogin(login: ItemEncrypted): String
    suspend fun saveLogins(logins: List<ItemEncrypted>)
    suspend fun importLogins(logins: List<Login>, triggerSync: Boolean = true)
    suspend fun getMostCommonUsernames(): List<String>
    suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Login>)
    suspend fun lockLogins()
    suspend fun unlockLogins()
    suspend fun permanentlyDeleteAll()
}