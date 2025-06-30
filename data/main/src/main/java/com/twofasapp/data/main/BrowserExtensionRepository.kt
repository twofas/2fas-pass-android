/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.ConnectData
import kotlinx.coroutines.flow.Flow

interface BrowserExtensionRepository {
    fun observeConnect(): Flow<ConnectData>
    fun publishConnect(connectData: ConnectData)
    fun observeRequests(): Flow<BrowserRequestData>
    suspend fun fetchRequests()
    fun deleteRequest(notificationId: String)
    suspend fun checkIsRequestValid(requestData: BrowserRequestData): Boolean
    suspend fun publishRequest(requestData: BrowserRequestData)
}