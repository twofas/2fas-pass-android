/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings

import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.booleanPref
import com.twofasapp.core.common.storage.longPref
import com.twofasapp.core.common.storage.serializedPrefNullable
import com.twofasapp.data.settings.domain.FailedAppUnlocks
import com.twofasapp.data.settings.local.model.FailedAppUnlocksEntity
import com.twofasapp.data.settings.mapper.asDomain
import com.twofasapp.data.settings.mapper.asEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SessionRepositoryImpl(
    private val dispatchers: Dispatchers,
    dataStoreOwner: DataStoreOwner,
) : SessionRepository, DataStoreOwner by dataStoreOwner {

    private val appVersionCode by longPref(default = 0L)
    private val startupCompleted by booleanPref(default = false)
    private val biometricsPrompted by booleanPref(default = false)
    private val connectOnboardingPrompted by booleanPref(default = false)
    private val quickSetupPrompted by booleanPref(default = true)
    private val appUpdatePrompted by booleanPref(default = false)
    private val failedAppUnlocks by serializedPrefNullable(
        serializer = FailedAppUnlocksEntity.serializer(),
        name = "failedAppUnlocks",
        encrypted = true,
    )

    override suspend fun getAppVersionCode(): Long {
        return withContext(dispatchers.io) { appVersionCode.get() }
    }

    override suspend fun setAppVersionCode(versionCode: Long) {
        withContext(dispatchers.io) { appVersionCode.set(versionCode) }
    }

    override fun observeStartupCompleted(): Flow<Boolean> {
        return startupCompleted.asFlow()
    }

    override suspend fun setStartupCompleted(completed: Boolean) {
        withContext(dispatchers.io) { startupCompleted.set(completed) }
    }

    override fun observeBiometricsPrompted(): Flow<Boolean> {
        return biometricsPrompted.asFlow()
    }

    override suspend fun setBiometricsPrompted(prompted: Boolean) {
        withContext(dispatchers.io) { biometricsPrompted.set(prompted) }
    }

    override fun observeConnectOnboardingPrompted(): Flow<Boolean> {
        return connectOnboardingPrompted.asFlow()
    }

    override suspend fun setConnectOnboardingPrompted(prompted: Boolean) {
        withContext(dispatchers.io) { connectOnboardingPrompted.set(prompted) }
    }

    override fun observeFailedAppUnlocks(): Flow<FailedAppUnlocks?> {
        return failedAppUnlocks.asFlow().map { it?.asDomain() }
    }

    override suspend fun setFailedAppUnlocks(failedAppUnlocks: FailedAppUnlocks?) {
        withContext(dispatchers.io) { this@SessionRepositoryImpl.failedAppUnlocks.set(failedAppUnlocks?.asEntity()) }
    }

    override fun observeQuickSetupPrompted(): Flow<Boolean> {
        return quickSetupPrompted.asFlow()
    }

    override suspend fun setQuickSetupPrompted(prompted: Boolean) {
        withContext(dispatchers.io) { quickSetupPrompted.set(prompted) }
    }

    override suspend fun getAppUpdatePrompted(): Boolean {
        return appUpdatePrompted.get()
    }

    override suspend fun setAppUpdatePrompted(prompted: Boolean) {
        withContext(dispatchers.io) { appUpdatePrompted.set(prompted) }
    }
}