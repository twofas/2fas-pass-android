/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.forms.login.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInRow
import com.twofasapp.core.design.foundation.text.TextIcon

@Composable
internal fun PasswordSuggestionsBar(
    modifier: Modifier = Modifier,
    onGenerateClick: () -> Unit = {},
    onOpenGeneratorClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(MdtTheme.color.surfaceContainerHigh)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextIcon(
            text = "Generator",
            leadingIcon = MdtIcons.PasswordGenerator,
            leadingIconTint = MdtTheme.color.primary,
            leadingIconSpacer = 8.dp,
            style = MdtTheme.typo.medium.base.copy(fontSize = 15.sp),
            color = MdtTheme.color.primary,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape)
                .clickable { onOpenGeneratorClick() }
                .padding(vertical = 8.dp),
        )

        VerticalDivider(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
        )

        TextIcon(
            text = "Auto-generate",
            leadingIcon = MdtIcons.Refresh,
            leadingIconTint = MdtTheme.color.primary,
            leadingIconSpacer = 6.dp,
            style = MdtTheme.typo.medium.base.copy(fontSize = 15.sp),
            color = MdtTheme.color.primary,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape)
                .clickable { onGenerateClick() }
                .padding(vertical = 8.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInRow {
        PasswordSuggestionsBar(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}