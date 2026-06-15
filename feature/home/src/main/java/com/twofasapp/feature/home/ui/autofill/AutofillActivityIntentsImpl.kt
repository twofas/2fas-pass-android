/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.inline.InlinePresentationSpec
import com.twofasapp.feature.autofill.service.builders.AutofillActivityIntents
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.autofill.service.parser.NodeStructure

class AutofillActivityIntentsImpl : AutofillActivityIntents {
    override fun createAuthPendingIntent(
        context: Context,
        nodeStructure: NodeStructure?,
        inlinePresentationSpec: InlinePresentationSpec?,
        login: AutofillLogin,
    ): PendingIntent {
        return AutofillActivity.createAuthPendingIntent(context, nodeStructure, inlinePresentationSpec, login)
    }

    override fun createPickerPendingIntent(
        context: Context,
        nodeStructure: NodeStructure,
        inlinePresentationSpec: InlinePresentationSpec?,
    ): PendingIntent {
        return AutofillActivity.createPickerPendingIntent(context, nodeStructure, inlinePresentationSpec)
    }

    override fun createSaveLoginIntent(
        context: Context,
        saveLoginData: SaveLoginData,
    ): Intent {
        return AutofillActivity.createSaveLoginIntent(context, saveLoginData)
    }
}