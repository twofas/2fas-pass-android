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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
fun ErrorState(
    title: String,
    text: String,
    onCtaClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = title,
            description = text,
            icon = MdtIcons.Error,
            iconTint = MdtTheme.color.primary,
        )

        Space(1f)

        Button(
            text = MdtLocale.strings.commonTryAgain,
            modifier = Modifier.fillMaxWidth(),
            onClick = onCtaClick,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ErrorState(
            title = "Error",
            text = "We were unable to read the backup file. It may be corrupt or damaged.",
        )
    }
}