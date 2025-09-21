package com.twofasapp.feature.itemform

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.feature.itemform.forms.login.LoginForm

@Composable
fun ItemForm(
    modifier: Modifier = Modifier,
    initialItem: Item,
    containerColor: Color = MdtTheme.color.background,
    confirmUnsavedChanges: Boolean = true,
    onItemUpdated: (Item) -> Unit = {},
    onIsValidUpdated: (Boolean) -> Unit = {},
    onHasUnsavedChangesUpdated: (Boolean) -> Unit = {},
    onCloseWithoutSaving: () -> Unit = {},
) {
    AnimatedContent(initialItem.content) { content ->
        when (content) {
            is ItemContent.Unknown -> Unit
            is ItemContent.Login -> {
                LoginForm(
                    modifier = modifier,
                    initialItem = initialItem,
                    initialItemContent = initialItem.content as ItemContent.Login,
                    containerColor = containerColor,
                    confirmUnsavedChanges = confirmUnsavedChanges,
                    onItemUpdated = onItemUpdated,
                    onIsValidUpdated = onIsValidUpdated,
                    onHasUnsavedChangesUpdated = onHasUnsavedChangesUpdated,
                    onCloseWithoutSaving = onCloseWithoutSaving,
                )
            }

            is ItemContent.SecureNote -> Unit
        }
    }
}