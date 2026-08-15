/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.logs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.showShareFilePicker
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.logs.domain.LogEntry
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LogsScreen(
    viewModel: LogsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val strings = MdtLocale.strings

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        uri?.let { fileUri ->
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                outputStream.write(viewModel.generateShareContent().toByteArray(Charsets.UTF_8))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.logsTitle,
                actions = {
                    if (uiState.logs.isNotEmpty()) {
                        if (uiState.isDebuggable) {
                            IconButton(
                                modifier = Modifier.testTag("logsClearButton"),
                                onClick = { viewModel.clearLogs() },
                            ) {
                                Icon(
                                    painter = MdtIcons.Delete,
                                    contentDescription = null,
                                )
                            }
                        }

                        IconButton(
                            modifier = Modifier.testTag("logsSaveButton"),
                            onClick = {
                                saveLauncher.launch(viewModel.generateFilename())
                            },
                        ) {
                            Icon(
                                painter = MdtIcons.Save,
                                contentDescription = null,
                            )
                        }

                        IconButton(
                            modifier = Modifier.testTag("logsShareButton"),
                            onClick = {
                                val filename = viewModel.generateFilename()
                                val content = viewModel.generateShareContent()
                                context.showShareFilePicker(
                                    filename = filename,
                                    title = strings.logsShareTitle,
                                    save = { outputStream ->
                                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                                    },
                                )
                            },
                        ) {
                            Icon(
                                painter = MdtIcons.Share,
                                contentDescription = strings.logsShareTitle,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.logs.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MdtTheme.color.background)
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.logsEmpty,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MdtTheme.color.background)
                    .padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(uiState.logs, key = { it.id }, contentType = { "Log" }) { entry ->
                    LogEntryItem(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: LogEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = entry.formattedTime,
                fontSize = 10.sp,
                color = MdtTheme.color.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 12.sp,
            )

            Text(
                text = entry.tag,
                fontSize = 10.sp,
                color = MdtTheme.color.primary.copy(alpha = 0.7f),
                lineHeight = 12.sp,
            )
        }

        Text(
            text = entry.message,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = MdtTheme.color.onSurface,
            lineHeight = 14.sp,
        )

        HorizontalDivider(
            color = MdtTheme.color.outline.copy(alpha = 0.2f),
            thickness = 0.5.dp,
        )
    }
}