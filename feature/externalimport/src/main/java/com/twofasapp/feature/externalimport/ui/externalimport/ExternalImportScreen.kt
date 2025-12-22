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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
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
    val strings = MdtLocale.strings

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
                        image = painterResource(uiState.importSpec.image),
                        text = buildAnnotatedString { append(strings.transferResultDescription) },
                        title = "${uiState.importSpec.name} ➞ 2FAS Pass",
                        cta = strings.transferResultCta,
                        loading = uiState.loading,
                        onCtaClick = { onStartImportClick(state.importContent) },
                        content = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ItemResultCount(
                                    icon = MdtIcons.Login,
                                    count = state.importContent.countLogins,
                                    subtitle = strings.transferResultLoginsDetected,
                                )

                                ItemResultCount(
                                    icon = MdtIcons.SecureNote,
                                    count = state.importContent.countSecureNotes,
                                    subtitle = strings.transferResultSecureNotesDetected,
                                )

                                ItemResultCount(
                                    icon = MdtIcons.Help,
                                    count = state.importContent.unknownItems,
                                    subtitle = strings.transferResultUnknownItems,
                                )

                                ItemResultCount(
                                    icon = MdtIcons.Tag,
                                    count = state.importContent.tags.size,
                                    subtitle = strings.transferResultTagsDetected,
                                )
                            }
                        },
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
    icon: Painter? = null,
    image: Painter? = null,
    title: String,
    text: AnnotatedString,
    cta: String,
    loading: Boolean,
    content: @Composable ColumnScope.() -> Unit = {},
    onCtaClick: () -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
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
                image = image,
            )

            Space(16.dp)

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

            Space(16.dp)

            content()
        }

        Button(
            text = cta,
            loading = loading,
            onClick = onCtaClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ItemResultCount(
    icon: Painter,
    count: Int,
    subtitle: String,
) {
    if (count > 0) {
        OptionEntry(
            icon = icon,
            title = count.toString(),
            subtitle = subtitle,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedShape12)
                .background(MdtTheme.color.surfaceContainer),
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

@Preview
@Composable
private fun PreviewResult() {
    PreviewTheme {
        Content(
            uiState = ExternalImportUiState(
                importState = ImportState.ReadSuccess(
                    ImportContent(
                        items = buildList {
                            addAll(List(15) { Item.create(ItemContentType.Login, ItemContent.Login.Empty) })
                            addAll(List(35) { Item.create(ItemContentType.SecureNote, ItemContent.SecureNote.Empty) })
                        },
                        tags = listOf(Tag.Empty, Tag.Empty),
                        unknownItems = 4,
                    ),
                ),
            ),
        )
    }
}