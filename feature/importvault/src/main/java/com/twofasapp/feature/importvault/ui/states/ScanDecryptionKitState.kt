/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.importvault.ui.states

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape24
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.qrscan.QrScan

@Composable
fun ScanDecryptionKitState(
    onScanned: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = MdtLocale.strings.scanDecryptionKitTitle,
            description = MdtLocale.strings.scanDecryptionKitDescription,
        )

        QrScan(
            modifier = Modifier
                .padding(vertical = ScreenPadding)
                .fillMaxSize()
                .clip(RoundedShape24),
            requireAuth = false,
            onScanned = { onScanned(it) },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ScanDecryptionKitState()
    }
}