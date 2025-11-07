/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.data.main.domain.ConnectedBrowser
import kotlinx.coroutines.flow.Flow

interface ConnectedBrowsersRepository {
    fun observeBrowsers(): Flow<List<ConnectedBrowser>>
    suspend fun hasAnyBrowsers(): Boolean
    suspend fun getBrowser(publicKey: ByteArray): ConnectedBrowser?
    suspend fun updateBrowser(browser: ConnectedBrowser)
    suspend fun deleteBrowser(browser: ConnectedBrowser)
    suspend fun permanentlyDeleteAll()
}