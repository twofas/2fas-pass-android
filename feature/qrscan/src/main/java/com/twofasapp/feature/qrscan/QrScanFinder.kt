/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.qrscan

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun QrScanFinder(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = R.drawable.img_qrscan_finder),
        contentDescription = null,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    QrScanFinder()
}