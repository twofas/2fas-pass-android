/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.builders

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT
import android.widget.inline.InlinePresentationSpec
import com.twofasapp.core.android.ktx.getSafelyParcelable
import com.twofasapp.core.android.ktx.getSafelyParcelableNullable
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import com.twofasapp.feature.autofill.ui.AutofillActivity
import java.security.SecureRandom

internal object IntentBuilders {
    const val EXTRA_START_SCREEN = "startScreen"
    const val EXTRA_NODE_STRUCTURE = "nodeStructure"
    const val EXTRA_INLINE_PRESENTATION_SPEC = "inlinePresentationSpec"
    const val EXTRA_LOGIN = "login"
    const val EXTRA_SAVE_LOGIN_DATA = "saveLoginData"

    enum class StartScreen {
        Authenticate, PickLogin, SaveLogin
    }

    @SuppressLint("NewApi")
    fun createAutofillAuthIntent(
        context: Context,
        nodeStructure: NodeStructure,
        inlinePresentationSpec: InlinePresentationSpec?,
        login: AutofillLogin,
    ): PendingIntent {
        val intent = Intent(context, AutofillActivity::class.java).apply {
            putExtra(EXTRA_NODE_STRUCTURE, nodeStructure)
            putExtra(EXTRA_INLINE_PRESENTATION_SPEC, inlinePresentationSpec)
            putExtra(EXTRA_LOGIN, login)
            putExtra(EXTRA_START_SCREEN, StartScreen.Authenticate)
        }

        return PendingIntent.getActivity(
            /* context = */
            context,
            /* requestCode = */
            login.id.hashCode(),
            /* intent = */
            intent,
            /* flags = */
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("NewApi")
    fun createAutofillPickerIntent(
        context: Context,
        nodeStructure: NodeStructure,
        inlinePresentationSpec: InlinePresentationSpec?,
    ): PendingIntent {
        val intent = Intent(context, AutofillActivity::class.java).apply {
            putExtra(EXTRA_NODE_STRUCTURE, nodeStructure)
            putExtra(EXTRA_INLINE_PRESENTATION_SPEC, inlinePresentationSpec)
            putExtra(EXTRA_START_SCREEN, StartScreen.PickLogin)
        }

        return PendingIntent.getActivity(
            /* context = */
            context,
            /* requestCode = */
            SecureRandom().nextInt(),
            /* intent = */
            intent,
            /* flags = */
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    @SuppressLint("NewApi")
    fun createSaveLoginIntent(
        context: Context,
        saveLoginData: SaveLoginData,
    ): Intent {
        return Intent(context, AutofillActivity::class.java).apply {
            putExtra(EXTRA_START_SCREEN, StartScreen.SaveLogin)
            putExtra(EXTRA_SAVE_LOGIN_DATA, saveLoginData)

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
    }

    fun Activity.replyWithSuccess(autofillLogin: AutofillLogin) {
        val nodeStructure = intent.extras.getSafelyParcelable<NodeStructure>(EXTRA_NODE_STRUCTURE)
        val inlinePresentationSpec = intent.extras.getSafelyParcelableNullable<InlinePresentationSpec>(EXTRA_INLINE_PRESENTATION_SPEC)

        val replyIntent = Intent().apply {
            putExtra(
                EXTRA_AUTHENTICATION_RESULT,
                DatasetBuilder.createLoginItem(
                    this@replyWithSuccess,
                    nodeStructure,
                    inlinePresentationSpec,
                    autofillLogin,
                ),
            )
        }

        setResult(RESULT_OK, replyIntent)
        finishAndRemoveTask()
    }
}