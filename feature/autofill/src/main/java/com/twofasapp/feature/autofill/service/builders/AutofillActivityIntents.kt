/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.builders

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.inline.InlinePresentationSpec
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.autofill.service.parser.NodeStructure

interface AutofillActivityIntents {
    @SuppressLint("NewApi")
    fun createAuthPendingIntent(
        context: Context,
        nodeStructure: NodeStructure?,
        inlinePresentationSpec: InlinePresentationSpec?,
        login: AutofillLogin,
    ): PendingIntent

    @SuppressLint("NewApi")
    fun createPickerPendingIntent(
        context: Context,
        nodeStructure: NodeStructure,
        inlinePresentationSpec: InlinePresentationSpec?,
    ): PendingIntent

    @SuppressLint("NewApi")
    fun createSaveLoginIntent(
        context: Context,
        saveLoginData: SaveLoginData,
    ): Intent
}