/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.servcies

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.twofasapp.core.common.services.CrashlyticsProvider

class CrashlyticsProviderFirebase : CrashlyticsProvider {
    private val crashlytics: FirebaseCrashlytics by lazy { Firebase.crashlytics }

    override fun setEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    override fun logException(e: Throwable?) {
        e?.let { crashlytics.recordException(it) }
    }
}