/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.itemdetails.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.LocalDarkMode
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.R
import com.twofasapp.core.design.feature.items.PaymentCardItemContentPreview
import com.twofasapp.core.design.feature.items.PaymentCardItemPreview
import com.twofasapp.core.design.feature.items.PaymentCardLogo
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.secretString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.SecretFieldType
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsEntry
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsPills
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun PaymentCardContent(
    item: Item,
    tags: ImmutableList<Tag>,
    content: ItemContent.PaymentCard,
    decryptedFields: Map<SecretFieldType, String>,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit,
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Image(
                painter = painterResource(
                    when (content.cardIssuer) {
                        ItemContent.PaymentCard.Issuer.Visa -> R.drawable.paymentcard_visa_bg
                        ItemContent.PaymentCard.Issuer.MasterCard -> R.drawable.paymentcard_mastercard_bg
                        ItemContent.PaymentCard.Issuer.AmericanExpress -> R.drawable.paymentcard_amex_bg
                        ItemContent.PaymentCard.Issuer.Discover -> R.drawable.paymentcard_discover_bg
                        ItemContent.PaymentCard.Issuer.DinersClub -> R.drawable.paymentcard_dinersclub_bg
                        ItemContent.PaymentCard.Issuer.Jcb -> R.drawable.paymentcard_jcb_bg
                        ItemContent.PaymentCard.Issuer.UnionPay -> R.drawable.paymentcard_unionpay_bg
                        null -> R.drawable.paymentcard_blank_bg
                    },
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 10f)
                    .clip(RoundedShape16)
                    .border(1.dp, MdtTheme.color.outlineVariant.copy(alpha = 0.7f), RoundedShape16),
            )

            Text(
                text = content.name,
                style = MdtTheme.typo.titleLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = Color(0xFFE2E1EE),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp),
            )

            Text(
                text = content.cardNumberMaskDisplayShort,
                style = MdtTheme.typo.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = Color(0xFFE2E1EE),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp, start = 24.dp),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 28.dp)
                    .size(42.dp)
                    .clip(RoundedShape12)
                    .background(Color(0xFF3B3B3B)),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(
                    LocalDarkMode provides true,
                ) {
                    PaymentCardLogo(
                        modifier = Modifier.fillMaxSize(),
                        cardIssuer = content.cardIssuer,
                    )
                }
            }
        }

        Space(16.dp)

        ItemDetailsPills(item = item, tags = tags)

        Space(24.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        if (content.cardHolder.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.cardHolderLabel,
                subtitle = content.cardHolder,
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("paymentCardContentCopyCardHolderButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.cardHolder.orEmpty()) },
                    )
                },
            )
        }

        content.cardNumber?.let { cardNumber ->
            ItemDetailsEntry(
                title = MdtLocale.strings.cardNumberLabel,
                subtitle = decryptedFields[SecretFieldType.PaymentCardNumber] ?: content.cardNumberMaskDisplayShort,
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.PaymentCardNumber] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.PaymentCardNumber, cardNumber) },
                        testTag = "paymentCardContentRevealCardNumberButton",
                    )

                    IconButton(
                        modifier = Modifier.testTag("paymentCardContentCopyCardNumberButton"),
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(cardNumber) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        content.expirationDate?.let { expirationDate ->
            ItemDetailsEntry(
                title = MdtLocale.strings.cardExpirationDateLabel,
                subtitle = decryptedFields[SecretFieldType.PaymentCardExpiration] ?: secretString(count = 5),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.PaymentCardExpiration] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.PaymentCardExpiration, expirationDate) },
                        testTag = "paymentCardContentRevealExpirationButton",
                    )

                    IconButton(
                        modifier = Modifier.testTag("paymentCardContentCopyExpirationButton"),
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(expirationDate) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        content.securityCode?.let { securityCode ->
            ItemDetailsEntry(
                title = MdtLocale.strings.cardSecurityCodeLabel,
                subtitle = decryptedFields[SecretFieldType.PaymentCardSecurityCode] ?: secretString(count = 3),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.PaymentCardSecurityCode] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.PaymentCardSecurityCode, securityCode) },
                        testTag = "paymentCardContentRevealSecurityCodeButton",
                    )

                    IconButton(
                        modifier = Modifier.testTag("paymentCardContentCopySecurityCodeButton"),
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(securityCode) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        if (content.notes.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.loginNotes,
                subtitle = content.notes.orEmpty(),
                isCompact = true,
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("paymentCardContentCopyNotesButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Column {
            Space(24.dp)

            PaymentCardContent(
                item = PaymentCardItemPreview,
                tags = persistentListOf(),
                content = PaymentCardItemContentPreview,
                decryptedFields = emptyMap(),
                onToggleSecretField = { _, _ -> },
                onCopySecretField = { _, _ -> },
            )
        }
    }
}