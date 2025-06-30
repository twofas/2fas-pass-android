/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.time

import android.os.SystemClock
import com.instacart.truetime.time.TrueTime
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.longPref
import com.twofasapp.core.common.time.TimeProvider
import kotlinx.coroutines.delay
import timber.log.Timber
import java.time.Duration
import java.time.Instant

internal class TimeProviderImpl(
    dataStoreOwner: DataStoreOwner,
    private val trueTime: TrueTime,
) : TimeProvider, DataStoreOwner by dataStoreOwner {

    private val savedDeltaMillis by longPref(default = 0L, name = "timeDeltaMillis")
    private var deltaMillis: Long = 0L
    private var isSynced: Boolean = false

    override fun systemElapsedTime(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun currentTimeUtc(): Long {
        return currentTimeUtcInstant().toEpochMilli()
    }

    override fun currentTimeUtcInstant(): Instant {
        return if (isSynced) {
            trueTime.nowSafely().toInstant()
        } else {
            Instant.now().plusMillis(deltaMillis)
        }
    }

    override suspend fun sync() {
        var syncDuration = 0L
        val retryDelay = 250L
        val retryTimeout = Duration.ofSeconds(20).toMillis()

        deltaMillis = savedDeltaMillis.get()

        Timber.d("TrueTime: sync started")

        trueTime.sync()

        while (isSynced.not() && syncDuration <= retryTimeout) {
            Timber.d("TrueTime: syncing...")

            if (trueTime.hasTheTime()) {
                deltaMillis = trueTime.nowSafely().toInstant().toEpochMilli() - Instant.now().toEpochMilli()
                isSynced = true
                savedDeltaMillis.set(deltaMillis)
            } else {
                delay(retryDelay)
                syncDuration += retryDelay
            }
        }

        if (isSynced) {
            Timber.d("TrueTime: sync finished, delta=$deltaMillis")
        } else {
            Timber.d("TrueTime: sync failed")
        }
    }
}