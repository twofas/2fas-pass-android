/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
fun EmptySearchResults(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = MdtLocale.strings.loginSearchNoResultsTitle,
            style = MdtTheme.typo.bold.base,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = MdtLocale.strings.loginSearchNoResultsDescription,
            style = MdtTheme.typo.regular.sm,
            color = MdtTheme.color.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        EmptySearchResults(
            modifier = Modifier.padding(16.dp),
        )
    }
}