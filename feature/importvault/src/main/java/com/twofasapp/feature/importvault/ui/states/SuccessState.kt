/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.importvault.ui.states

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale

@Composable
fun SuccessState(
    onCtaClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Space(1f)

        Image(
            painter = painterResource(com.twofasapp.core.design.R.drawable.img_success),
            contentDescription = null,
            modifier = Modifier.height(120.dp),
        )

        Space(32.dp)

        ScreenHeader(
            title = MdtLocale.strings.restoreSuccessTitle,
            description = MdtLocale.strings.restoreSuccessDescription,
        )

        Space(1f)

        Button(
            text = MdtLocale.strings.restoreSuccessCta,
            modifier = Modifier.fillMaxWidth(),
            onClick = onCtaClick,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        SuccessState()
    }
}