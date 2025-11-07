/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.data.main.domain.ConnectedBrowser
import com.twofasapp.data.main.local.ConnectedBrowsersLocalSource
import com.twofasapp.data.main.mapper.ConnectedBrowserMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class ConnectedBrowsersRepositoryImpl(
    private val dispatchers: Dispatchers,
    private val androidKeyStore: AndroidKeyStore,
    private val localSource: ConnectedBrowsersLocalSource,
    private val connectedBrowserMapper: ConnectedBrowserMapper,
) : ConnectedBrowsersRepository {

    override fun observeBrowsers(): Flow<List<ConnectedBrowser>> {
        return localSource.observeConnectedBrowsers()
            .map { list ->
                val appKey = androidKeyStore.appKey

                list.map { connectedBrowserMapper.mapToDomain(entity = it, appKey = appKey) }
            }
            .flowOn(dispatchers.io)
    }

    override suspend fun hasAnyBrowsers(): Boolean {
        return withContext(dispatchers.io) {
            localSource.hasAnyBrowsers()
        }
    }

    override suspend fun getBrowser(publicKey: ByteArray): ConnectedBrowser? {
        return withContext(dispatchers.io) {
            getConnectedBrowsers().firstOrNull { it.publicKey.contentEquals(publicKey) }
        }
    }

    override suspend fun updateBrowser(browser: ConnectedBrowser) {
        withContext(dispatchers.io) {
            localSource.saveConnectedBrowser(
                connectedBrowserMapper.mapToEntity(
                    domain = browser,
                    appKey = androidKeyStore.appKey,
                ),
            )
        }
    }

    override suspend fun deleteBrowser(browser: ConnectedBrowser) {
        withContext(dispatchers.io) {
            localSource.deleteConnectedBrowser(browser.id)
        }
    }

    override suspend fun permanentlyDeleteAll() {
        withContext(dispatchers.io) {
            localSource.deleteAll()
        }
    }

    private suspend fun getConnectedBrowsers(): List<ConnectedBrowser> {
        val appKey = androidKeyStore.appKey

        return localSource.getConnectedBrowsers()
            .map { connectedBrowserMapper.mapToDomain(entity = it, appKey = appKey) }
    }
}