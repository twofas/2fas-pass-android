/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.twofasapp.core.android.ktx.screenRoute
import com.twofasapp.core.android.navigation.NavArgKey
import com.twofasapp.core.android.navigation.ScreenType

@Composable
internal fun NavHandler(
    navController: NavHostController,
    onCurrentRouteChanged: (String?) -> Unit,
    onBottomBarVisibilityChanged: (Boolean) -> Unit,
) {
    val mainNavListener = remember { MainNavListener() }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val currentScreenRoute by remember {
        derivedStateOf {
            currentBackStackEntry?.destination?.screenRoute
        }
    }

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener(mainNavListener)
    }

    LaunchedEffect(currentScreenRoute) {
        onCurrentRouteChanged(currentScreenRoute)

        @Suppress("DEPRECATION")
        val screenType = currentBackStackEntry?.arguments
            ?.getSerializable(NavArgKey.ScreenType) as? ScreenType

        onBottomBarVisibilityChanged(
            when (screenType) {
                ScreenType.TopLevel -> true
                ScreenType.WithBottomBar -> true
                ScreenType.Standard -> false
                null -> false
            },
        )
    }
}