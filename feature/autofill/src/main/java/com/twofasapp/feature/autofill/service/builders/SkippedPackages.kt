/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.builders

internal object SkippedPackages {
    val packages = arrayOf(
        "com.android.settings",
        "com.android.systemui",
        "com.google.android.googlequicksearchbox",
    )

    fun isSkipped(packageName: String?): Boolean {
        val name = packageName.orEmpty()
        return packages.any { name.startsWith(it) }
    }
}