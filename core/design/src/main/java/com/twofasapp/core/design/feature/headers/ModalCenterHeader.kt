/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.headers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInColumn
import com.twofasapp.core.design.foundation.preview.PreviewTextMedium

@Composable
fun ModalCenterHeader(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    descriptionAnnotated: AnnotatedString? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MdtTheme.typo.headlineSmall,
            color = MdtTheme.color.onSurface,
            textAlign = TextAlign.Center,
        )

        description?.let {
            Space(8.dp)

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        descriptionAnnotated?.let {
            Space(8.dp)

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInColumn {
        ScreenHeader(
            title = "Headline",
            description = PreviewTextMedium,
            image = MdtIcons.Placeholder,
        )
    }
}