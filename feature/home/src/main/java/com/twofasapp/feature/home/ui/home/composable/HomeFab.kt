/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.home.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.MdtIcons

@Composable
internal fun HomeFab(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClick: () -> Unit = {},
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = scaleIn(tween(150)),
        exit = scaleOut(tween(150)),
    ) {
        FloatingActionButton(
            onClick = onClick,
            content = { Icon(painter = MdtIcons.Add, contentDescription = null) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    HomeFab()
}