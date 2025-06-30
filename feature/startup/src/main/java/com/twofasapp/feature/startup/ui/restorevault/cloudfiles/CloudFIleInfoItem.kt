/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.restorevault.cloudfiles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.ktx.formatDateTime
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewAllThemesInColumn
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.domain.CloudFileInfo
import java.time.Duration
import java.time.Instant

@Composable
fun CloudFilInfoItem(
    modifier: Modifier = Modifier,
    item: CloudFileInfo,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainer)
            .clickable { onClick() }
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "ID: ${item.vaultId}",
                style = MdtTheme.typo.labelSmall,
                color = MdtTheme.color.onSurfaceVariant.copy(alpha = 0.5f),
            )

            Space(6.dp)

            Text(
                text = item.deviceName,
                style = MdtTheme.typo.titleMedium,
            )

            Space(2.dp)

            Text(
                text = buildAnnotatedString {
                    append(MdtLocale.strings.restoreCloudFilesUpdatedAt.format(""))

                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        if ((Instant.now().toEpochMilli() - item.vaultUpdatedAt.toEpochMilli()) >= Duration.ofDays(1).toMillis()) {
                            append(item.vaultUpdatedAt.formatDateTime())
                        } else {
                            append(MdtLocale.strings.formatDuration(item.vaultUpdatedAt.toEpochMilli()))
                        }
                    }
                },
                style = MdtTheme.typo.bodyMedium,
                color = MdtTheme.color.onSurfaceVariant,
            )
        }

        Icon(
            painter = MdtIcons.ChevronRight,
            contentDescription = null,
            tint = MdtTheme.color.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewAllThemesInColumn {
        CloudFilInfoItem(
            item = CloudFileInfo.GoogleDrive(
                fileId = "",
                deviceId = "1234567890",
                deviceName = "Samsung S23",
                seedHashHex = "",
                vaultId = "4260fcca-9d38-4eae-8766-a7865b0e64d5",
                vaultCreatedAt = Instant.now().minusSeconds(Duration.ofDays(1).toSeconds()),
                vaultUpdatedAt = Instant.now().minusSeconds(Duration.ofDays(1).toSeconds()),
                schemaVersion = 1,
            ),
        )
    }
}