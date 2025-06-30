/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.push

import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.twofasapp.core.android.ktx.resumeIfActive
import com.twofasapp.core.android.ktx.resumeWithExceptionIfActive
import com.twofasapp.core.common.push.PushTokenProvider
import kotlinx.coroutines.suspendCancellableCoroutine

internal class FcmTokenProvider : PushTokenProvider {
    override suspend fun provide(): String = suspendCancellableCoroutine { continuation ->
        Firebase.messaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resumeIfActive(it.result)
            } else {
                continuation.resumeWithExceptionIfActive(it.exception ?: RuntimeException("Error when fetching FCM token."))
            }
        }
    }
}