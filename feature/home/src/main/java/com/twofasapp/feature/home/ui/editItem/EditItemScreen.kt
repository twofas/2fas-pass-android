/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.editItem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.feature.items.LoginItemPreview
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.ItemForm
import com.twofasapp.feature.itemform.ItemFormListener
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun EditItemScreen(
    viewModel: EditItemViewModel = koinViewModel(),
    close: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onItemUpdated = viewModel::updateItem,
        onIsValidUpdated = viewModel::updateIsValid,
        onHasUnsavedChangesUpdated = viewModel::updateHasUnsavedChanges,
        onSaveClick = { viewModel.save(onComplete = close) },
        onCloseWithoutSaving = close,
    )
}

@Composable
private fun Content(
    uiState: EditItemUiState,
    onItemUpdated: (Item) -> Unit = {},
    onIsValidUpdated: (Boolean) -> Unit = {},
    onHasUnsavedChangesUpdated: (Boolean) -> Unit = {},
    onSaveClick: () -> Unit = {},
    onCloseWithoutSaving: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = if (uiState.item.id.isBlank()) {
                    strings.loginAddTitle
                } else {
                    strings.loginEditTitle
                },
                actions = {
                    Button(
                        text = strings.commonSave,
                        style = ButtonStyle.Text,
                        onClick = onSaveClick,
                        enabled = uiState.isValid && uiState.hasUnsavedChanges,
                    )
                },
            )
        },
    ) { padding ->
        if (uiState.initialItem != null) {
            ItemForm(
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
                initialItem = uiState.initialItem,
                listener = object : ItemFormListener {
                    override fun onItemUpdated(item: Item) {
                        onItemUpdated(item)
                    }

                    override fun onIsValidUpdated(valid: Boolean) {
                        onIsValidUpdated(valid)
                    }

                    override fun onHasUnsavedChangesUpdated(hasUnsavedChanges: Boolean) {
                        onHasUnsavedChangesUpdated(hasUnsavedChanges)
                    }

                    override fun onCloseWithoutSaving() {
                        onCloseWithoutSaving()
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(appTheme = AppTheme.Dark) {
        Content(
            uiState = EditItemUiState(
                initialItem = LoginItemPreview,
                item = LoginItemPreview,
            ),
        )
    }
}