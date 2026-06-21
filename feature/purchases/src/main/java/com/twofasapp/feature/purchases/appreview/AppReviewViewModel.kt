/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.purchases.appreview

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.settings.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

internal class AppReviewViewModel(
    context: Context,
    private val authStatusTracker: AuthStatusTracker,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val reviewManager = ReviewManagerFactory.create(context)

    fun maybeRequestReview(activity: Activity) {
        launchScoped {
            try {
                if (isEligible().not()) return@launchScoped

                delay(PromptDelayMillis)

                val reviewInfo = reviewManager.requestReviewFlow().await()
                reviewManager.launchReviewFlow(activity, reviewInfo).await()
                sessionRepository.markAppReviewPrompted()
            } catch (e: Exception) {
                Flog.tag("AppReview").e("Failed to launch in-app review flow", e)
            }
        }
    }

    private suspend fun isEligible(): Boolean {
        if (sessionRepository.getAppReviewPromptedAt().toEpochMilli() > 0L) return false

        if (authStatusTracker.isAuthenticated().not()) return false

        return true
    }

    companion object {
        private const val PromptDelayMillis = 500L
    }
}