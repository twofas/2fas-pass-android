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
import com.twofasapp.core.common.logger.Flog
import com.twofasapp.data.settings.SessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class AppReviewViewModel(
    context: Context,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val reviewManager = ReviewManagerFactory.create(context)

    fun rate(activity: Activity) {
        launchScoped {
            try {
                val reviewInfo = reviewManager.requestReviewFlow().await()
                reviewManager.launchReviewFlow(activity, reviewInfo).await()
            } catch (e: Exception) {
                Flog.tag("AppReview").e("Failed to launch in-app review flow", e)
            } finally {
                delay(500)
                sessionRepository.markAppReviewPrompted()
            }
        }
    }

    fun dismiss() {
        launchScoped {
            sessionRepository.markAppReviewPrompted()
        }
    }
}