/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.commonmodal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.progress.CircularProgress
import com.twofasapp.core.design.foundation.progress.CircularProgressSize

@Composable
internal fun LoadingState(
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 29.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgress(
            size = CircularProgressSize.Large,
        )

        Text(
            text = text,
            style = MdtTheme.typo.bodySmall,
            color = MdtTheme.color.onSurfaceVariant,
        )
    }
}