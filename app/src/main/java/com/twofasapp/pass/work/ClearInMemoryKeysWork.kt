/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.twofasapp.core.android.ktx.cancel
import com.twofasapp.core.android.ktx.enqueueUniqueAndReplace
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.main.DerivedKeysRepository
import com.twofasapp.data.main.VaultKeysRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ClearInMemoryKeysWork(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val vaultKeysRepository: VaultKeysRepository by inject()
    private val derivedKeysRepository: DerivedKeysRepository by inject()

    companion object {
        fun dispatch(context: Context, delayMillis: Long) {
            context.enqueueUniqueAndReplace<ClearInMemoryKeysWork>(
                request = OneTimeWorkRequestBuilder<ClearInMemoryKeysWork>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .build(),
            )
        }

        fun cancel(context: Context) {
            context.cancel<ClearInMemoryKeysWork>()
        }
    }

    override suspend fun doWork(): Result {
        Flog.persist("ClearKeys", "ClearInMemoryKeys: started")
        if (isStopped) {
            Flog.persist("ClearKeys", "ClearInMemoryKeys: stopped before run")
            return Result.success()
        }

        vaultKeysRepository.clearInMemoryVaultKeys()
        derivedKeysRepository.clearInMemoryDerivedKeys()

        Flog.persist("ClearKeys", "ClearInMemoryKeys: completed")
        return Result.success()
    }
}