package com.twofasapp.feature.itemform.forms.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.feature.itemform.ItemFormListener
import com.twofasapp.feature.itemform.ItemFormProperties
import com.twofasapp.feature.itemform.ItemFormUiState
import com.twofasapp.feature.itemform.ItemFormViewModel

@Composable
internal fun <T : ItemContent> ItemContentFormContainer(
    viewModel: ItemFormViewModel<T>,
    initialItem: Item,
    properties: ItemFormProperties,
    listener: ItemFormListener,
    content: @Composable (uiState: ItemFormUiState<T>) -> Unit,
) {
    val uiState by viewModel.itemState.collectAsStateWithLifecycle()

    FormBackHandler(
        itemUiState = uiState,
        confirmUnsavedChanges = properties.shouldConfirmUnsavedChanges,
        onCloseWithoutSaving = { listener.onCloseWithoutSaving() },
    )

    LaunchedEffect(initialItem) {
        viewModel.init(initialItem = initialItem)
    }

    if (uiState.initialised) {
        LaunchedEffect(uiState.item) {
            listener.onItemUpdated(uiState.item)
        }

        LaunchedEffect(uiState.valid) {
            listener.onIsValidUpdated(uiState.valid)
        }

        LaunchedEffect(uiState.hasUnsavedChanges) {
            listener.onHasUnsavedChangesUpdated(uiState.hasUnsavedChanges)
        }

        content(uiState)
    }
}