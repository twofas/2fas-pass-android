/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.twofasapp.core.android.ktx.navigateTopLevel
import com.twofasapp.core.android.ktx.screenRoute
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.R
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInRow
import com.twofasapp.core.locale.MdtLocale

private data class NavItem(
    val screen: Screen,
    val title: String,
    val iconRes: Int,
    val showDot: Boolean,
)

@Composable
internal fun MainBottomBar(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    cloudSyncError: Boolean,
) {
    val strings = MdtLocale.strings
    val currentBackStack by navController.currentBackStack.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember {
        derivedStateOf {
            currentBackStackEntry?.destination?.screenRoute
        }
    }

    val bottomNavItems = remember(cloudSyncError) {
        listOf(
            NavItem(
                screen = Screen.Home(),
                title = strings.bottomBarPasswords,
                iconRes = R.drawable.ic_key,
                showDot = false,
            ),
            NavItem(
                screen = Screen.Connect(),
                title = strings.bottomBarConnect,
                iconRes = R.drawable.ic_encrypted,
                showDot = false,
            ),
            NavItem(
                screen = Screen.Settings(),
                title = strings.bottomBarSettings,
                iconRes = R.drawable.ic_settings,
                showDot = cloudSyncError,
            ),
        )
    }

    val startScreen by remember {
        derivedStateOf { bottomNavItems.first().screen }
    }

    val bottomNavRoutes by remember {
        derivedStateOf { bottomNavItems.map { it.screen.route } }
    }

    BackHandler(enabled = bottomNavRoutes.contains(currentRoute) && currentRoute != startScreen.route) {
        navController.navigateTopLevel(startScreen)
    }

    NavigationBar(
        modifier = modifier,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentBackStack.findMostRecentTopLevelScreenRoute(bottomNavRoutes) == item.screen.route

            Item(
                text = item.title,
                icon = painterResource(item.iconRes),
                showDot = item.showDot,
                selected = selected,
                onClick = {
                    if (selected.not()) {
                        navController.navigateTopLevel(item.screen)
                    }
                },
            )
        }
    }
}

private fun List<NavBackStackEntry>.findMostRecentTopLevelScreenRoute(topLevelRoutes: List<String>): String? {
    return lastOrNull { topLevelRoutes.contains(it.destination.screenRoute) }?.destination?.screenRoute
}

@Composable
private fun RowScope.Item(
    text: String,
    icon: Painter,
    selected: Boolean,
    showDot: Boolean = false,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MdtTheme.typo.semiBold.xxs.copy(color = Color.Unspecified)) },
        icon = {
            Box {
                Icon(
                    painter = icon,
                    contentDescription = text,
                )

                if (showDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MdtTheme.color.error),
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInRow {
        MainBottomBar(
            modifier = Modifier.fillMaxWidth(),
            navController = rememberNavController(),
            cloudSyncError = true,
        )
    }
}