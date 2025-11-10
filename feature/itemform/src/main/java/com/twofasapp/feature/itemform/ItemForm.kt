package com.twofasapp.feature.itemform

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.feature.itemform.forms.login.LoginForm
import com.twofasapp.feature.itemform.forms.securenote.SecureNoteForm

@Composable
fun ItemForm(
    modifier: Modifier = Modifier,
    initialItem: Item,
    containerColor: Color = MdtTheme.color.background,
    properties: ItemFormProperties = ItemFormProperties.Default,
    listener: ItemFormListener,
) {
    AnimatedContent(initialItem.content) { content ->
        when (content) {
            is ItemContent.Unknown -> Unit
            is ItemContent.Login -> {
                LoginForm(
                    modifier = modifier,
                    initialItem = initialItem,
                    containerColor = containerColor,
                    properties = properties,
                    listener = listener,
                )
            }

            is ItemContent.SecureNote -> {
                SecureNoteForm(
                    modifier = modifier,
                    initialItem = initialItem,
                    containerColor = containerColor,
                    properties = properties,
                    listener = listener,
                )
            }

            is ItemContent.CreditCard -> Unit
        }
    }
}