/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.ui.externalimport

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.toastLong
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.ui.template.ImportTemplate
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ExternalImportScreen(
    viewModel: ExternalImportViewModel = koinViewModel(),
    openLogins: () -> Unit,
) {
    val context = LocalContext.currentActivity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onFilePicked = { viewModel.readContent(it) },
        onStartImportClick = {
            viewModel.startImport(it) {
                context.toastLong("Import successful!")
                openLogins()
            }
        },
        onTryAgainClick = { viewModel.tryAgain() },
    )
}

@Composable
private fun Content(
    uiState: ExternalImportUiState,
    onFilePicked: (Uri) -> Unit = {},
    onStartImportClick: (ImportContent) -> Unit = {},
    onTryAgainClick: () -> Unit = {},
) {
    Scaffold(
        topBar = { TopAppBar() },
    ) { padding ->

        AnimatedContent(uiState.importState, label = "importState") { state ->
            when (state) {
                ImportState.Default -> {
                    ImportTemplate(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = padding.calculateTopPadding())
                            .padding(horizontal = ScreenPadding)
                            .padding(bottom = ScreenPadding),
                        importSpec = uiState.importSpec,
                        loading = uiState.loading,
                        onFilePicked = onFilePicked,
                    )
                }

                is ImportState.ReadSuccess -> {
                    ImportStateResult(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = padding.calculateTopPadding())
                            .padding(ScreenPadding),
                        icon = MdtIcons.Document,
                        text = buildAnnotatedString {
                            append("You are going to import ")

                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MdtTheme.color.primary)) {
                                append("${state.importContent.items.size} services")
                            }

                            append(" from ${uiState.importSpec.name}.")
                            append("\n\n")

                            if (state.importContent.skipped > 0) {
                                append("${state.importContent.skipped} services could not be imported because they have an incompatible format.")
                                append("\n\n")
                            }

                            append("The file will be synchronised with the app\'s service list.")
                        },
                        title = "Start Transfer",
                        cta = "Proceed",
                        loading = uiState.loading,
                        onCtaClick = { onStartImportClick(state.importContent) },
                    )
                }

                is ImportState.Error -> {
                    ImportStateResult(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = padding.calculateTopPadding())
                            .padding(ScreenPadding),
                        icon = MdtIcons.Error,
                        text = buildAnnotatedString {
                            append("An error occurred while trying to read the file. Please check that the file content is correct and try again.")
                            append("\n\n")
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MdtTheme.color.error,
                                ),
                            ) {
                                append("Error: ${state.msg}")
                            }
                        },
                        title = "Transfer Error",
                        cta = "Try again",
                        loading = uiState.loading,
                        onCtaClick = onTryAgainClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportStateResult(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    text: AnnotatedString,
    cta: String,
    loading: Boolean,
    onCtaClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenHeader(
                title = title,
                icon = icon,
            )

            Space(24.dp)

            SelectionContainer {
                Text(
                    text = text,
                    style = MdtTheme.typo.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }
        }

        Button(
            text = cta,
            loading = loading,
            onClick = onCtaClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = ExternalImportUiState(),
        )
    }
}