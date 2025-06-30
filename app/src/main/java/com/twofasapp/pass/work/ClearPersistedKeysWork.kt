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
import com.twofasapp.data.main.VaultKeysRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ClearPersistedKeysWork(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val vaultKeysRepository: VaultKeysRepository by inject()

    companion object {
        fun dispatch(context: Context, delayMillis: Long) {
            context.enqueueUniqueAndReplace<ClearPersistedKeysWork>(
                request = OneTimeWorkRequestBuilder<ClearPersistedKeysWork>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .build(),
            )
        }

        fun cancel(context: Context) {
            context.cancel<ClearPersistedKeysWork>()
        }
    }

    override suspend fun doWork(): Result {
        if (isStopped) return Result.success()

        vaultKeysRepository.clearPersistedVaultKeys()

        return Result.success()
    }
}