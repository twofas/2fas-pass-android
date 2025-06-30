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
import com.twofasapp.core.android.ktx.enqueueUniqueAndReplace
import com.twofasapp.data.settings.SettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class DisableScreenCaptureWork(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val settingsRepository: SettingsRepository by inject()

    companion object {
        fun dispatch(context: Context) {
            context.enqueueUniqueAndReplace<DisableScreenCaptureWork>(
                request = OneTimeWorkRequestBuilder<DisableScreenCaptureWork>()
                    .setInitialDelay(5, TimeUnit.MINUTES)
                    .build(),
            )
        }
    }

    override suspend fun doWork(): Result {
        settingsRepository.setScreenCaptureEnabled(false)

        return Result.success()
    }
}