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
    val currentScreenRouteArguments by remember {
        derivedStateOf {
            currentBackStackEntry?.arguments ?: Bundle.EMPTY
        }
    }

    val bottomBarVisible by remember {
        derivedStateOf {
            @Suppress("DEPRECATION")
            when (currentScreenRouteArguments.getSerializable(NavArgKey.ScreenType) as? ScreenType) {
                ScreenType.Standard -> false
                ScreenType.TopLevel -> true
                ScreenType.WithBottomBar -> true
                null -> false
            }
        }
    }

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener(mainNavListener)
    }

    LaunchedEffect(bottomBarVisible) {
        onBottomBarVisibilityChanged(bottomBarVisible)
    }

    LaunchedEffect(currentScreenRoute) {
        onCurrentRouteChanged(currentScreenRoute)
    }
}