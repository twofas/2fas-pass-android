/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.trash

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.screen.LazyContent
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.state.ScreenState
import com.twofasapp.core.design.theme.RoundedTopShape
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.purchases.PurchasesDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun TrashScreen(
    viewModel: TrashViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        screenState = screenState,
        onItemToggled = { viewModel.toggle(it) },
        onSelectAll = { viewModel.selectAll() },
        onClearSelections = { viewModel.clearSelections() },
        onRestoreClick = { viewModel.restore() },
        onDeleteConfirmed = { viewModel.delete() },
    )
}

@Composable
private fun Content(
    uiState: TrashUiState,
    screenState: ScreenState,
    onItemToggled: (Item) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelections: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val strings = MdtLocale.strings
    val onBackDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = if (uiState.hasSelections) "${uiState.selected.size} selected" else strings.settingsEntryTrash,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasSelections) {
                                onClearSelections()
                            } else {
                                onBackDispatcher?.onBackPressed()
                            }
                        },
                    ) {
                        Icon(
                            painter = if (uiState.hasSelections) MdtIcons.Close else MdtIcons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    if (uiState.trashedItems.isNotEmpty()) {
                        ActionsRow {
                            IconButton(
                                icon = MdtIcons.CheckAll,
                                onClick = onSelectAll,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyContent(
                screenState = screenState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MdtTheme.color.background),
                contentPadding = PaddingValues(bottom = if (uiState.selected.isEmpty()) 0.dp else 2 * ScreenPadding + 40.dp),
                itemsWhenSuccess = {
                    uiState.trashedItems.forEach { item ->
                        item(key = item.id, contentType = "Item") {
                            TrashItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(fadeInSpec = null, fadeOutSpec = null),
                                item = item,
                                checked = uiState.selected.contains(item.id),
                                onCheckedChange = { onItemToggled(item) },
                            )
                        }
                    }
                },
                emptyIcon = MdtIcons.Delete,
            )

            AnimatedVisibility(
                visible = uiState.hasSelections,
                enter = slideInVertically(initialOffsetY = { it / 2 }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MdtTheme.color.surfaceContainer, RoundedTopShape)
                        .padding(ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = {
                            if (uiState.itemsCount + uiState.selected.size >= uiState.maxItems) {
                                showPaywall = true
                            } else {
                                onRestoreClick()
                                scope.launch {
                                    delay(300)
                                    snackbarHostState.showSnackbar("Items restored!")
                                }
                            }
                        },
                        content = {
                            TextIcon(
                                text = "Restore",
                                leadingIcon = MdtIcons.Restore,
                                leadingIconTint = MdtTheme.color.onPrimary,
                            )
                        },
                    )

                    Button(
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onClick = { showDeleteDialog = true },
                        content = {
                            TextIcon(
                                text = "Delete",
                                leadingIcon = MdtIcons.DeleteForever,
                                leadingIconTint = MdtTheme.color.onPrimary,
                            )
                        },
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Delete ${uiState.selected.size} ${if (uiState.selected.size == 1) "item" else "items"}?",
            body = "Are you sure you want to permanently delete ${uiState.selected.size} ${if (uiState.selected.size == 1) "item" else "items"}? This cannot be undone!",
            icon = MdtIcons.DeleteForever,
            onPositive = {
                onDeleteConfirmed()
                scope.launch {
                    delay(300)
                    snackbarHostState.showSnackbar("Items deleted!")
                }
            },
        )
    }

    if (showPaywall) {
        PurchasesDialog(
            title = MdtLocale.strings.paywallNoticeItemsLimitRestoreTitle,
            body = MdtLocale.strings.paywallNoticeItemsLimitRestoreMsg.format(uiState.maxItems),
            onDismissRequest = { showPaywall = false },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
//        Content(
//            uiState = TrashUiState(trashedItems = listOf(Login.Preview), selected = listOf("")),
//            screenState = ScreenState.Success,
//        )
    }
}