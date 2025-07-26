/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.builders

import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.Field
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import com.twofasapp.feature.autofill.R
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.parser.AutofillInput
import com.twofasapp.feature.autofill.service.parser.NodeStructure

internal object DatasetBuilder {

    fun createLoginItem(
        context: Context,
        nodeStructure: NodeStructure?,
        inlinePresentationSpec: InlinePresentationSpec?,
        login: AutofillLogin,
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        val pendingIntent = IntentBuilders.createAutofillAuthIntent(
            context = context,
            nodeStructure = nodeStructure,
            inlinePresentationSpec = inlinePresentationSpec,
            login = login,
        )

        val remoteView = loginItemRemoteView(context, login)

        val inlinePresentation = InlinePresentationBuilder.create(
            title = login.name,
            subtitle = login.username,
            icon = if (login.encrypted) {
                Icon.createWithResource(context, R.drawable.autofill_login_encrypted_icon)
            } else {
                Icon.createWithResource(context, R.drawable.autofill_login_icon)
            },
            spec = inlinePresentationSpec,
            pendingIntent = pendingIntent,
        )

        nodeStructure?.inputs.orEmpty().forEach { input ->
            when (input) {
                is AutofillInput.Username -> {
                    datasetBuilder.setField(
                        autofillId = input.id,
                        autofillText = login.username,
                        remoteView = remoteView,
                        inlinePresentation = inlinePresentation,
                    )
                }

                is AutofillInput.Password -> {
                    datasetBuilder.setField(
                        autofillId = input.id,
                        autofillText = login.password,
                        remoteView = remoteView,
                        inlinePresentation = inlinePresentation,
                    )
                }
            }
        }

        if (login.encrypted) {
            datasetBuilder.setAuthentication(pendingIntent.intentSender)
        }

        return datasetBuilder.build()
    }

    fun createAppItem(
        context: Context,
        nodeStructure: NodeStructure,
        inlinePresentationSpec: InlinePresentationSpec?,
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        val pendingIntent = IntentBuilders.createAutofillPickerIntent(
            context = context,
            nodeStructure = nodeStructure,
            inlinePresentationSpec = inlinePresentationSpec,
        )

        val remoteView = appItemRemoteView(context)
        val inlinePresentation = InlinePresentationBuilder.create(
            title = "2FAS Pass",
            subtitle = "Open",
            icon = Icon.createWithResource(context, R.drawable.autofill_app_icon),
            spec = inlinePresentationSpec,
            pendingIntent = pendingIntent,
        )

        nodeStructure.inputs.forEach { input ->
            when (input) {
                is AutofillInput.Username -> {
                    datasetBuilder.setField(
                        autofillId = input.id,
                        autofillText = null,
                        remoteView = remoteView,
                        inlinePresentation = inlinePresentation,
                    )
                }

                is AutofillInput.Password -> {
                    datasetBuilder.setField(
                        autofillId = input.id,
                        autofillText = null,
                        remoteView = remoteView,
                        inlinePresentation = inlinePresentation,
                    )
                }
            }
        }

        datasetBuilder.setAuthentication(pendingIntent.intentSender)

        return datasetBuilder.build()
    }

    fun createPinnedItem(
        context: Context,
        nodeStructure: NodeStructure,
        inlinePresentationSpec: InlinePresentationSpec?,
    ): Dataset {
        val datasetBuilder = Dataset.Builder()

        val pendingIntent = IntentBuilders.createAutofillPickerIntent(
            context = context,
            nodeStructure = nodeStructure,
            inlinePresentationSpec = inlinePresentationSpec,
        )

        val remoteView = appItemRemoteView(context)
        val inlinePresentation = InlinePresentationBuilder.create(
            icon = Icon.createWithResource(context, R.drawable.autofill_app_icon),
            pinned = true,
            spec = inlinePresentationSpec,
            pendingIntent = pendingIntent,
        )

        nodeStructure.inputs.forEach { input ->
            when (input) {
                is AutofillInput.Username -> {
                    datasetBuilder.setField(
                        autofillId = input.id,
                        autofillText = null,
                        remoteView = remoteView,
                        inlinePresentation = inlinePresentation,
                    )
                }

                is AutofillInput.Password -> {
                    datasetBuilder.setField(
                        autofillId = input.id,
                        autofillText = null,
                        remoteView = remoteView,
                        inlinePresentation = inlinePresentation,
                    )
                }
            }
        }

        datasetBuilder.setAuthentication(pendingIntent.intentSender)

        return datasetBuilder.build()
    }

    private fun Dataset.Builder.setField(
        autofillId: AutofillId?,
        autofillText: String? = null,
        remoteView: RemoteViews,
        inlinePresentation: InlinePresentation? = null,
    ): Dataset.Builder {
        if (autofillId == null) return this

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val fieldBuilder = Field.Builder()

            if (autofillText != null) {
                fieldBuilder.setValue(AutofillValue.forText(autofillText))
            }

            val presentations = Presentations.Builder().apply {
                setMenuPresentation(remoteView)
                if (inlinePresentation != null) {
                    setInlinePresentation(inlinePresentation)
                }
            }.build()

            fieldBuilder.setPresentations(presentations)

            setField(autofillId, fieldBuilder.build())
        } else if (inlinePresentation != null) {
            @Suppress("DEPRECATION")
            setValue(autofillId, AutofillValue.forText(autofillText), remoteView, inlinePresentation)
        } else {
            @Suppress("DEPRECATION")
            setValue(autofillId, AutofillValue.forText(autofillText), remoteView)
        }
    }
}