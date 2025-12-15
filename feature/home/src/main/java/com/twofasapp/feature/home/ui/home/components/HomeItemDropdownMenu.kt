package com.twofasapp.feature.home.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.locale.MdtLocale

@Composable
internal fun HomeItemDropdownMenu(
    item: Item,
    onDetailsClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onCopySecretFieldToClipboard: (SecretField?) -> Unit = {},
    onTrashClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showDropdown by remember { mutableStateOf(false) }

    DropdownMenu(
        visible = showDropdown,
        onDismissRequest = { showDropdown = false },
        anchor = {
            IconButton(
                icon = MdtIcons.More,
                iconTint = MdtTheme.color.outline,
                onClick = { showDropdown = true },
            )
        },
        content = {
            DropdownMenuItem(
                text = MdtLocale.strings.homeItemView,
                icon = MdtIcons.Visibility,
                onClick = {
                    showDropdown = false
                    onDetailsClick()
                },
            )

            DropdownMenuItem(
                text = MdtLocale.strings.homeItemEdit,
                icon = MdtIcons.Edit,
                onClick = {
                    showDropdown = false
                    onEditClick()
                },
            )

            item.content.let { content ->
                when (content) {
                    is ItemContent.Unknown -> Unit
                    is ItemContent.Login -> {
                        DropdownMenuItem(
                            text = MdtLocale.strings.homeItemCopyUsername,
                            icon = MdtIcons.User,
                            onClick = {
                                showDropdown = false
                                context.copyToClipboard(content.username.orEmpty())
                            },
                        )

                        DropdownMenuItem(
                            text = MdtLocale.strings.homeItemCopyPassword,
                            icon = MdtIcons.Key,
                            onClick = {
                                showDropdown = false
                                onCopySecretFieldToClipboard(content.password)
                            },
                        )

                        DropdownMenuItem(
                            text = MdtLocale.strings.homeItemOpenUri,
                            icon = MdtIcons.Open,
                            onClick = {
                                showDropdown = false
                                uriHandler.openSafely(content.uris.firstOrNull()?.text, context)
                            },
                        )
                    }

                    is ItemContent.SecureNote -> {
                        DropdownMenuItem(
                            text = "Copy note",
                            icon = MdtIcons.Document,
                            onClick = {
                                showDropdown = false
                                onCopySecretFieldToClipboard(content.text)
                            },
                        )
                    }

                    is ItemContent.PaymentCard -> {
                        DropdownMenuItem(
                            text = "Copy card number",
                            icon = MdtIcons.PaymentCard,
                            onClick = {
                                showDropdown = false
                                onCopySecretFieldToClipboard(content.cardNumber)
                            },
                        )
                    }
                }
            }

            DropdownMenuItem(
                text = MdtLocale.strings.homeItemDelete,
                icon = MdtIcons.Delete,
                contentColor = MdtTheme.color.error,
                onClick = {
                    showDropdown = false
                    onTrashClick()
                },
            )
        },
    )
}