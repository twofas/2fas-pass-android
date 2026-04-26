/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.push

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.twofasapp.core.android.ktx.resumeIfActive
import com.twofasapp.core.android.ktx.resumeWithExceptionIfActive
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.core.common.push.PushTokenProvider
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FcmTokenProvider : PushTokenProvider {
    override suspend fun provide(): String = suspendCancellableCoroutine { continuation ->
        Firebase.messaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                Flog.persist("Push", "FCM token fetched")
                continuation.resumeIfActive(it.result)
            } else {
                val exception = it.exception ?: RuntimeException("Error when fetching FCM token.")
                Flog.persist("Push", "FCM token fetch failed: ${exception.message}")
                Flog.persist("Push", exception)
                continuation.resumeWithExceptionIfActive(exception)
            }
        }
    }
}