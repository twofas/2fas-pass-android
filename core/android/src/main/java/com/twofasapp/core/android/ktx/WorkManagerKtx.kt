/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.ktx

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import timber.log.Timber

inline fun <reified T : CoroutineWorker> Context.enqueueUniqueAndReplace(request: OneTimeWorkRequest) {
    Timber.tag("WorkManager").d("Schedule: ${T::class.java.simpleName}")
    WorkManager.getInstance(this).enqueueUniqueWork(T::class.java.simpleName, ExistingWorkPolicy.REPLACE, request)
}

inline fun <reified T : CoroutineWorker> Context.enqueueUniqueIfNotScheduled(request: OneTimeWorkRequest) {
    if (isThereUniqueWorkScheduled(T::class.java.simpleName).not()) {
        Timber.tag("WorkManager").d("Schedule: ${T::class.java.simpleName}")
        WorkManager.getInstance(this).enqueueUniqueWork(T::class.java.simpleName, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}

fun Context.isThereUniqueWorkScheduled(uniqueWorkName: String): Boolean {
    return WorkManager.getInstance(this).getWorkInfosForUniqueWork(uniqueWorkName).get()
        .find { it.state == WorkInfo.State.BLOCKED || it.state == WorkInfo.State.ENQUEUED }?.let {
            Timber.tag("WorkManager").d("There is a work (${it.id}) in queue - do not schedule new one")
            true
        } ?: false
}

inline fun <reified T : CoroutineWorker> Context.cancel() {
    Timber.tag("WorkManager").d("Cancel: ${T::class.java.simpleName}")
    WorkManager.getInstance(this).cancelUniqueWork(T::class.java.simpleName)
}