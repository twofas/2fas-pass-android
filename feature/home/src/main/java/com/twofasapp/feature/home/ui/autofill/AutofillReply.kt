/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.widget.inline.InlinePresentationSpec
import androidx.appcompat.app.AppCompatActivity
import com.twofasapp.core.android.ktx.getSafelyParcelable
import com.twofasapp.feature.autofill.service.builders.AutofillActivityIntents
import com.twofasapp.feature.autofill.service.builders.DatasetBuilder
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import org.koin.android.ext.android.get

fun AppCompatActivity.replyWithAutofillSuccess(autofillLogin: AutofillLogin) {
    val autofillActivityIntents = get<AutofillActivityIntents>()
    val nodeStructure = intent.extras.getSafelyParcelable<NodeStructure>(AutofillActivity.EXTRA_NODE_STRUCTURE)
    val inlinePresentationSpec = intent.extras.getSafelyParcelable<InlinePresentationSpec>(AutofillActivity.EXTRA_INLINE_PRESENTATION_SPEC)

    val replyIntent = Intent().apply {
        putExtra(
            EXTRA_AUTHENTICATION_RESULT,
            DatasetBuilder.createLoginItem(
                context = this@replyWithAutofillSuccess,
                autofillActivityIntents = autofillActivityIntents,
                nodeStructure = nodeStructure,
                inlinePresentationSpec = inlinePresentationSpec,
                login = autofillLogin,
            ),
        )
    }

    setResult(RESULT_OK, replyIntent)
    finishAndRemoveTask()
}