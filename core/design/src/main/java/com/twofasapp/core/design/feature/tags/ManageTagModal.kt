/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.TagColor
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.dialog.InputValidation
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.modal.ModalHeaderProperties
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.android.awaitFrame
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ManageTagModal(
    tag: Tag,
    onSave: (Tag) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ProvidesViewModelStoreOwner {
        ManageTagModalInternal(
            tag = tag,
            onSave = onSave,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun ManageTagModalInternal(
    tag: Tag,
    viewModel: ManageTagViewModel = koinViewModel { parametersOf(tag) },
    onSave: (Tag) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ManageTagModal(
        uiState = uiState,
        onSaveClick = { onSave(uiState.tag) },
        onDismissRequest = onDismissRequest,
        onNameChane = viewModel::onNameChanged,
        onColorClick = viewModel::onColorSelected
    )
}

@Composable
private fun ManageTagModal(
    uiState: ManageTagUiState,
    onNameChane: (String) -> Unit,
    onColorClick: (TagColor) -> Unit,
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        awaitFrame()
        focusRequester.requestFocus()
    }

    Modal(
        onDismissRequest = onDismissRequest,
        headerText = when (uiState.mode) {
            ManageTagModalMode.Add -> MdtLocale.strings.tagEditorNewTitle
            ManageTagModalMode.Edit -> MdtLocale.strings.tagEditorEditTitle
        },
        headerProperties = ModalHeaderProperties(
            showCloseButton = true,
        ),
    ) { dismissAction ->

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = MdtLocale.strings.tagEditorDescription
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = uiState.tag.name,
            onValueChange = onNameChane,
            labelText = MdtLocale.strings.tagEditorPlaceholder,
            placeholderText = MdtLocale.strings.tagEditorPlaceholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            maxLines = 1,
            supportingText = (uiState.nameValidation as? InputValidation.Invalid)?.error.orEmpty(),
            isError = uiState.nameValidation is InputValidation.Invalid,
            trailingIcon = {
                Icon(
                    painter = MdtIcons.Tag,
                    contentDescription = null,
                    tint = uiState.tag.color?.iconTint() ?: MdtTheme.color.outlineVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            colors(
                colors = uiState.colors,
                selectedColor = uiState.tag.color,
                onClick = onColorClick
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = uiState.nameValidation == InputValidation.Valid && uiState.colorValidation == InputValidation.Valid,
            text = MdtLocale.strings.commonContinue,
            onClick = { dismissAction { onSaveClick() } }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun LazyListScope.colors(
    colors: ImmutableList<TagColor>,
    selectedColor: TagColor?,
    onClick: (TagColor) -> Unit
) {
    items(items = colors, key = { it.value }) { item ->
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick(item) })
                .background(if (item == selectedColor) Color(0xFF30CFBB) else MdtTheme.color.outlineVariant)
                .padding(1.dp)
                .clip(CircleShape)
                .background(MdtTheme.color.surfaceContainerLow)
                .padding(2.dp)
                .clip(CircleShape)
                .background(item.iconTint())
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ManageTagModal(
            uiState = ManageTagUiState(
                tag = Tag.Empty.copy(name = "Tag name"),
                colors = TagColor.values().toPersistentList(),
                nameValidation = null,
                colorValidation = null,
                mode = ManageTagModalMode.Add,
            ),
            onNameChane = {},
            onSaveClick = {},
            onDismissRequest = {},
            onColorClick = {}
        )
    }
}