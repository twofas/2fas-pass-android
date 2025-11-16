/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.securitytype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.lazy.forEachIndexed
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.text.richText
import com.twofasapp.core.design.theme.RoundedShapeIndexed
import com.twofasapp.core.locale.MdtLocale

@Composable
fun SecurityTypeModal(
    onDismissRequest: () -> Unit,
    selected: SecurityType?,
    onSelect: (SecurityType) -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.securityTypeModalHeader,
    ) { dismissAction ->
        Content(
            selected = selected,
            onSelect = { dismissAction { onSelect(it) } },
        )
    }
}

@Composable
private fun Content(
    selected: SecurityType?,
    onSelect: (SecurityType) -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        SecurityType.entries.reversed().forEachIndexed { _, isFirst, isLast, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedShapeIndexed(isFirst, isLast))
                    .background(MdtTheme.color.surfaceContainerHigh)
                    .clickable { onSelect(item) }
                    .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
            ) {
                Icon(
                    painter = item.asIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MdtTheme.color.primary,
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.asTitle(),
                        style = MdtTheme.typo.medium.base,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = richText(item.asDescription()),
                        style = MdtTheme.typo.regular.sm,
                        color = MdtTheme.color.onSurfaceVariant,
                    )
                }

                CheckIcon(
                    checked = selected == item,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = MdtLocale.strings.securityTypeModalDescription,
            style = MdtTheme.typo.regular.xs,
            color = MdtTheme.color.secondary,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
internal fun SecurityType.asIcon(): Painter {
    return when (this) {
        SecurityType.Tier1 -> MdtIcons.Tier1
        SecurityType.Tier2 -> MdtIcons.Tier2
        SecurityType.Tier3 -> MdtIcons.Tier3
    }
}

@Composable
fun SecurityType.asTitle(): String {
    return when (this) {
        SecurityType.Tier1 -> MdtLocale.strings.settingsEntrySecurityTier1
        SecurityType.Tier2 -> MdtLocale.strings.settingsEntrySecurityTier2
        SecurityType.Tier3 -> MdtLocale.strings.settingsEntrySecurityTier3
    }
}

@Composable
internal fun SecurityType.asDescription(): String {
    return when (this) {
        SecurityType.Tier1 -> MdtLocale.strings.settingsEntrySecurityTier1Desc
        SecurityType.Tier2 -> MdtLocale.strings.settingsEntrySecurityTier2Desc
        SecurityType.Tier3 -> MdtLocale.strings.settingsEntrySecurityTier3Desc
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        Content(
            selected = SecurityType.Tier2,
        )
    }
}