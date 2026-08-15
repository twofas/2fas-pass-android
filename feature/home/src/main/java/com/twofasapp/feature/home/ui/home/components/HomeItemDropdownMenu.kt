package com.twofasapp.feature.home.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
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
    onShareClick: () -> Unit = {},
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
                modifier = Modifier.testTag("homeItemMenuButton"),
                icon = MdtIcons.More,
                iconTint = MdtTheme.color.outline,
                onClick = { showDropdown = true },
            )
        },
        content = {
            DropdownMenuItem(
                text = MdtLocale.strings.homeItemView,
                leadingIcon = MdtIcons.Visibility,
                onClick = {
                    showDropdown = false
                    onDetailsClick()
                },
            )

            DropdownMenuItem(
                text = MdtLocale.strings.homeItemEdit,
                leadingIcon = MdtIcons.Edit,
                onClick = {
                    showDropdown = false
                    onEditClick()
                },
            )

            DropdownMenuItem(
                text = MdtLocale.strings.commonShare,
                leadingIcon = MdtIcons.Share,
                onClick = {
                    showDropdown = false
                    onShareClick()
                },
            )

            item.content.let { content ->
                when (content) {
                    is ItemContent.Unknown -> Unit
                    is ItemContent.Login -> {
                        content.username.takeIf { it.isNullOrEmpty().not() }?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.homeItemCopyUsername,
                                leadingIcon = MdtIcons.User,
                                onClick = {
                                    showDropdown = false
                                    context.copyToClipboard(content.username.orEmpty())
                                },
                            )
                        }
                        content.password?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.homeItemCopyPassword,
                                leadingIcon = MdtIcons.Key,
                                onClick = {
                                    showDropdown = false
                                    onCopySecretFieldToClipboard(content.password)
                                },
                            )
                        }

                        content.uris.firstOrNull()?.text.takeIf { it.isNullOrEmpty().not() }
                            ?.let { uri ->
                                DropdownMenuItem(
                                    text = MdtLocale.strings.homeItemOpenUri,
                                    leadingIcon = MdtIcons.Open,
                                    onClick = {
                                        showDropdown = false
                                        uriHandler.openSafely(uri, context)
                                    },
                                )
                            }
                    }

                    is ItemContent.SecureNote -> {
                        content.text?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.secureNoteViewActionCopy,
                                leadingIcon = MdtIcons.Document,
                                onClick = {
                                    showDropdown = false
                                    onCopySecretFieldToClipboard(content.text)
                                },
                            )
                        }
                    }

                    is ItemContent.PaymentCard -> {
                        content.cardNumber?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.cardViewActionCopyCardNumber,
                                leadingIcon = MdtIcons.PaymentCard,
                                onClick = {
                                    showDropdown = false
                                    onCopySecretFieldToClipboard(content.cardNumber)
                                },
                            )
                        }

                        content.expirationDate?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.cardViewActionCopyExpirationDate,
                                leadingIcon = MdtIcons.PaymentCardDate,
                                onClick = {
                                    showDropdown = false
                                    onCopySecretFieldToClipboard(content.expirationDate)
                                },
                            )
                        }

                        content.securityCode?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.cardViewActionCopySecurityCode,
                                leadingIcon = MdtIcons.PaymentCardCode,
                                onClick = {
                                    showDropdown = false
                                    onCopySecretFieldToClipboard(content.securityCode)
                                },
                            )
                        }
                    }

                    is ItemContent.Wifi -> {
                        content.ssid.takeIf { it.isNullOrEmpty().not() }?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.wifiViewActionCopySsid,
                                leadingIcon = MdtIcons.Wifi4Bar,
                                onClick = {
                                    showDropdown = false
                                    context.copyToClipboard(content.ssid.orEmpty())
                                },
                            )
                        }
                        content.password?.let {
                            DropdownMenuItem(
                                text = MdtLocale.strings.wifiViewActionCopyPassword,
                                leadingIcon = MdtIcons.Key,
                                onClick = {
                                    showDropdown = false
                                    onCopySecretFieldToClipboard(content.password)
                                },
                            )
                        }
                    }
                }
            }

            DropdownMenuItem(
                text = MdtLocale.strings.homeItemDelete,
                leadingIcon = MdtIcons.Delete,
                contentColor = MdtTheme.color.error,
                onClick = {
                    showDropdown = false
                    onTrashClick()
                },
            )
        },
    )
}