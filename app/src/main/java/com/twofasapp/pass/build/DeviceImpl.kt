/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.build

import android.os.Build
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.crypto.Uuid
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.stringPrefNullable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DeviceImpl(
    dataStoreOwner: DataStoreOwner,
) : DataStoreOwner by dataStoreOwner, Device {

    companion object {
        private val DefaultName: String = Build.MANUFACTURER + " " + Build.MODEL
    }

    private val deviceId by stringPrefNullable(encrypted = true)
    private val deviceName by stringPrefNullable(encrypted = true)

    override suspend fun uniqueId(): String {
        return deviceId.get() ?: Uuid.generate().also { deviceId.set(it) }
    }

    override suspend fun name(): String {
        return deviceName.get() ?: DefaultName
    }

    override fun observeName(): Flow<String> {
        return deviceName.asFlow().map { it ?: DefaultName }
    }

    override suspend fun setName(name: String?) {
        deviceName.set(name)
    }
}