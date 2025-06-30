/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import com.twofasapp.feature.autofill.service.handlers.FillRequestHandler
import com.twofasapp.feature.autofill.service.handlers.SaveRequestHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class PassAutofillService : AutofillService(), KoinComponent {
    private val fillRequestHandler: FillRequestHandler by inject()
    private val saveRequestHandler: SaveRequestHandler by inject()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        internal const val AutofillTag = "PassAutofillService"
    }

    override fun onConnected() {
        super.onConnected()
        Timber.tag(AutofillTag).d(
            """
            =================================================
            🚀 onConnected
            """.trimIndent(),
        )
    }

    override fun onFillRequest(
        fillRequest: FillRequest,
        cancellationSignal: CancellationSignal,
        fillCallback: FillCallback,
    ) {
        Timber.tag(AutofillTag).d("\uD83D\uDD20 onFillRequest: ${fillRequest.id}")

        val fillRequestJob = scope.launch {
            fillRequestHandler.handleRequest(
                context = this@PassAutofillService,
                fillRequest = fillRequest,
                fillCallback = fillCallback,
            )
        }

        cancellationSignal.setOnCancelListener {
            Timber.tag(AutofillTag).d("☠\uFE0F onCancelRequest: ${fillRequest.id}")
            fillRequestJob.cancel()
        }
    }

    override fun onSaveRequest(
        saveRequest: SaveRequest,
        saveCallback: SaveCallback,
    ) {
        Timber.tag(AutofillTag).d("\uD83D\uDCBE onSaveRequest")

        scope.launch {
            saveRequestHandler.handleRequest(
                context = this@PassAutofillService,
                saveRequest = saveRequest,
                saveCallback = saveCallback,
            )
        }
    }

    override fun onDestroy() {
        Timber.tag(AutofillTag).d(
            """
            ☠️ onDestroy
            =================================================
            """.trimIndent(),
        )
        super.onDestroy()
        scope.cancel()
    }
}