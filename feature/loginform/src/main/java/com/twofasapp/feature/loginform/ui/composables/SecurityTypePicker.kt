/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.loginform.ui.modal.SecurityTierModal
import com.twofasapp.feature.loginform.ui.modal.asIcon
import com.twofasapp.feature.loginform.ui.modal.asTitle

@Composable
internal fun SecurityTypePicker(
    modifier: Modifier = Modifier,
    securityType: SecurityType,
    onOpened: () -> Unit = {},
    onSelect: (SecurityType) -> Unit = {},
) {
    var showSecurityLevelModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainer)
            .clickable {
                onOpened()
                showSecurityLevelModal = true
            }
            .padding(vertical = 8.dp)
            .padding(start = 12.dp, end = 4.dp),
    ) {
        OptionEntry(
            icon = securityType.asIcon(),
            title = MdtLocale.strings.loginSecurityLevel,
            subtitle = securityType.asTitle(),
            content = {
                Icon(
                    painter = MdtIcons.ChevronRight,
                    contentDescription = null,
                    tint = MdtTheme.color.onSurface,
                )
            },
            contentPadding = ZeroPadding,
        )
    }

    if (showSecurityLevelModal) {
        SecurityTierModal(
            onDismissRequest = { showSecurityLevelModal = false },
            onSelect = { onSelect(it) },
            selected = securityType,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        SecurityTypePicker(
            modifier = Modifier.fillMaxWidth(),
            securityType = SecurityType.Tier3,
        )
    }
}