/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import timber.log.Timber

internal class MainNavListener : NavController.OnDestinationChangedListener {
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        log(destination, arguments)
    }

    private fun log(destination: NavDestination, arguments: Bundle?) {
        val argumentsLog: String = if (destination.arguments.isEmpty()) {
            ""
        } else {
            "args=" + destination.arguments.map {
                @Suppress("DEPRECATION")
                "${it.key}=${arguments?.get(it.key)}"
            }.toString()
        }

        Timber.tag("NavController").d("route=${destination.route} $argumentsLog")
    }
}