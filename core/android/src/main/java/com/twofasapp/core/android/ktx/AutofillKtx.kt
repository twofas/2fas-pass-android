/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.ktx

import android.view.autofill.AutofillManager

fun AutofillManager?.hasEnabledAutofillServicesSafely(): Boolean {
    return try {
        this?.hasEnabledAutofillServices() ?: false
    } catch (e: Exception) {
        false
    }
}