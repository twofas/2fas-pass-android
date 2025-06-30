/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.domain.EncryptedLogin
import com.twofasapp.core.common.domain.Login
import com.twofasapp.data.main.domain.CloudMerge
import kotlinx.coroutines.flow.Flow

interface LoginsRepository {
    fun observeLogins(vaultId: String): Flow<List<EncryptedLogin>>
    suspend fun getLogin(id: String): EncryptedLogin
    suspend fun getLogins(): List<EncryptedLogin>
    suspend fun getLoginsDecrypted(): List<Login>
    suspend fun getLoginsDecryptedWithDeleted(): List<Login>
    suspend fun getLoginsCount(): Int
    suspend fun saveLogin(login: EncryptedLogin): String
    suspend fun saveLogins(logins: List<EncryptedLogin>)
    suspend fun importLogins(logins: List<Login>, triggerSync: Boolean = true)
    suspend fun getMostCommonUsernames(): List<String>
    suspend fun executeCloudMerge(cloudMerge: CloudMerge.Result<Login>)
    suspend fun lockLogins()
    suspend fun unlockLogins()
    suspend fun permanentlyDeleteAll()
}