/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.lifecycle

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.data.settings.domain.AutofillLockTime
import com.twofasapp.pass.work.ClearInMemoryKeysWork
import com.twofasapp.pass.work.ClearPersistedKeysWork
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

internal class AppLifecycleObserver(
    private val context: Context,
    private val authTracker: AuthStatusTracker,
    private val settingsRepository: SettingsRepository,
) : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.tag("AppLifecycleObserver").i("onAppForeground")

        owner.lifecycleScope.launch {
            authTracker.onAppForeground()

            ClearInMemoryKeysWork.cancel(context)
            ClearPersistedKeysWork.cancel(context)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.tag("AppLifecycleObserver").i("onAppBackground")

        owner.lifecycleScope.launch {
            authTracker.onAppBackground()

            ClearInMemoryKeysWork.dispatch(context, delayMillis = settingsRepository.observeAppLockTime().first().millis)

            settingsRepository.observeAutofillLockTime().first().let { delay ->
                when (delay) {
                    AutofillLockTime.Never -> Unit
                    else -> ClearPersistedKeysWork.dispatch(context, delayMillis = delay.millis)
                }
            }
        }
    }
}