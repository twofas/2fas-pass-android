package com.twofasapp.feature.itemform.forms.common

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.dialog.ConfirmDialog
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.ItemFormUiState

@Composable
internal fun FormBackHandler(
    itemUiState: ItemFormUiState<*>,
    confirmUnsavedChanges: Boolean,
    onCloseWithoutSaving: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = itemUiState.hasUnsavedChanges && confirmUnsavedChanges) {
        focusManager.clearFocus()
        showUnsavedChangesDialog = true
    }

    if (showUnsavedChangesDialog) {
        ConfirmDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            onPositive = onCloseWithoutSaving,
            icon = MdtIcons.Warning,
            title = MdtLocale.strings.loginUnsavedChangesDialogTitle,
            body = MdtLocale.strings.loginUnsavedChangesDialogDescription,
        )
    }
}