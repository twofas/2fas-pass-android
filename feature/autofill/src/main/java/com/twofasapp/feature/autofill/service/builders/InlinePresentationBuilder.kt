/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

@file:Suppress("DEPRECATION")

package com.twofasapp.feature.autofill.service.builders

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.slice.Slice
import android.graphics.BlendMode
import android.graphics.drawable.Icon
import android.service.autofill.InlinePresentation
import android.widget.inline.InlinePresentationSpec
import androidx.autofill.inline.v1.InlineSuggestionUi

@SuppressLint("RestrictedApi")
internal object InlinePresentationBuilder {

    fun create(
        title: String? = null,
        subtitle: String? = null,
        icon: Icon? = null,
        pinned: Boolean = false,
        spec: InlinePresentationSpec?,
        pendingIntent: PendingIntent,
    ): InlinePresentation? {
        if (spec == null) return null

        val slice = createSlice(
            title = title,
            subtitle = subtitle,
            icon = icon,
            pendingIntent = pendingIntent,
        )

        return InlinePresentation(slice, spec, pinned)
    }

    private fun createSlice(
        title: String? = null,
        subtitle: String? = null,
        icon: Icon? = null,
        pendingIntent: PendingIntent,
    ): Slice {
        val builder = InlineSuggestionUi.newContentBuilder(pendingIntent)
            .setContentDescription(title ?: "Pinned")

        if (title.isNullOrBlank().not()) {
            builder.setTitle(title.orEmpty().take(16))
        }

        if (subtitle.isNullOrBlank().not()) {
            builder.setSubtitle(subtitle.orEmpty().take(16))
        }

        if (icon != null) {
            icon.setTintBlendMode(BlendMode.DST)
            builder.setStartIcon(icon)
        }

        return builder.build().slice
    }
}