/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.ktx

import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Dp.toPx() = with(LocalDensity.current) { toPx() }

@Composable
fun Dp.roundToPx() = with(LocalDensity.current) { roundToPx() }

@Composable
fun Int.toDp() = with(LocalDensity.current) { toDp() }

@Composable
fun Float.toDp() = with(LocalDensity.current) { toDp() }

val bottomBarHeight = 80.dp

val bottomBarHeightPx: Float
    @Composable
    get() = bottomBarHeight.toPx()

val CompositionLocal<Density>.statusBarHeight: Int
    @Composable
    get() = WindowInsets.statusBars.getTop(current)

val CompositionLocal<Density>.navigationBarsHeight: Int
    @Composable
    get() = WindowInsets.navigationBars.getBottom(current)

val screenHeight: Dp
    @Composable
    get() = LocalConfiguration.current.screenHeightDp.dp

val screenWidth: Dp
    @Composable
    get() = LocalConfiguration.current.screenWidthDp.dp

val screenHeightPx: Float
    @Composable
    get() = screenHeight.toPx()

val screenWidthPx: Float
    @Composable
    get() = screenWidth.toPx()

val statusBarHeight: Dp
    @Composable
    get() = statusBarHeightPx.toDp()

val statusBarHeightPx: Int
    @Composable
    get() = WindowInsets.statusBars.getTop(LocalDensity.current)

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}

/*
* Return bottom Ime padding as state
* */
@Composable
fun keyboardOffsetAsState(): State<Int> {
    return rememberUpdatedState(WindowInsets.ime.getBottom(LocalDensity.current))
}

/*
* Return bottom Ime padding as state excluding navigation bar
* */
@Composable
fun keyboardOffsetWithoutNavigationAsState(): State<Int> {
    return rememberUpdatedState(
        maxOf(
            0,
            WindowInsets.ime.getBottom(LocalDensity.current) - WindowInsets.navigationBars.getBottom(LocalDensity.current),
        ),
    )
}

/*
* Return bottom Ime padding as state excluding navigation bar and bottom bar
* */
@Composable
fun keyboardOffsetWithoutBottomBarAsState(): State<Int> {
    return rememberUpdatedState(
        maxOf(
            0,
            WindowInsets.ime.getBottom(LocalDensity.current) - WindowInsets.navigationBars.getBottom(LocalDensity.current) - bottomBarHeightPx.toInt(),
        ),
    )
}

enum class ScreenOrientation { Portrait, Landscape }

val Configuration.screenOrientation: ScreenOrientation
    get() = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.Landscape
        else -> ScreenOrientation.Portrait
    }