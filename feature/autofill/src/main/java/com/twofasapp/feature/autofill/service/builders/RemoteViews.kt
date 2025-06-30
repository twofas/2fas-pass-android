/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.builders

import android.content.Context
import android.widget.RemoteViews
import com.twofasapp.feature.autofill.R
import com.twofasapp.feature.autofill.service.domain.AutofillLogin

internal fun loginItemRemoteView(context: Context, login: AutofillLogin): RemoteViews {
    return RemoteViews(context.packageName, R.layout.autofill_menu_item).apply {
        if (login.encrypted) {
            setImageViewResource(R.id.icon, R.drawable.autofill_login_encrypted_icon)
        } else {
            setImageViewResource(R.id.icon, R.drawable.autofill_login_icon)
        }

        setTextViewText(R.id.title, login.name)
        setTextViewText(R.id.subtitle, login.username)
    }
}

internal fun appItemRemoteView(context: Context): RemoteViews {
    return RemoteViews(context.packageName, R.layout.autofill_menu_item).apply {
        setImageViewResource(R.id.icon, R.drawable.autofill_app_icon)
        setTextViewText(R.id.title, "2FAS Pass")
        setTextViewText(R.id.subtitle, "Open App")
    }
}