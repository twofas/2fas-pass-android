/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import.spec

import android.content.Context
import android.net.Uri
import com.twofasapp.core.common.domain.ImportType
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.locale.R
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.externalimport.import.CsvParser
import com.twofasapp.feature.externalimport.import.ImportContent
import com.twofasapp.feature.externalimport.import.ImportSpec
import com.twofasapp.feature.externalimport.import.ZipFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class AppleMobileImportSpec(
    private val vaultsRepository: VaultsRepository,
    private val context: Context,
    private val json: Json,
) : ImportSpec() {
    override val type = ImportType.AppleDesktop
    override val name = "Apple Passwords (Mobile)"
    override val image = com.twofasapp.core.design.R.drawable.external_logo_apple
    override val instructions = context.getString(R.string.transfer_instructions_apple_passwords_mobile)
    override val additionalInfo = null
    override val cta: List<Cta> = listOf(
        Cta.Primary(
            text = context.getString(R.string.transfer_instructions_cta_zip),
            action = CtaAction.ChooseFile,
        ),
    )

    override suspend fun readContent(uri: Uri): ImportContent {
        val vaultId = vaultsRepository.getVault().id
        val items = mutableListOf<Item>()
        var unknownItems = 0

        val zipFile = ZipFile(
            uri = uri,
        )

        val csvFileContents = zipFile.read(
            context = context,
            filter = { filename -> filename.endsWith(".csv", true) },
        )

        csvFileContents.values.forEach { file ->
            CsvParser.parse(
                text = file,
            ) { row ->
                items.add(
                    Item.create(
                        vaultId = vaultId,
                        contentType = ItemContentType.Login,
                        content = ItemContent.Login.create(
                            name = row.get("Title"),
                            username = row.get("Username"),
                            password = row.get("Password"),
                            url = row.get("URL"),
                            notes = row.get("Notes"),
                        ),
                    ),
                )
            }
        }

        // Import payment cards from JSON if present
        val jsonFileContents = zipFile.read(
            context = context,
            filter = { filename -> filename.endsWith(".json", true) },
        )

        jsonFileContents.values.forEach { file ->
            val paymentCardItems = parsePaymentCards(file, vaultId)
            items.addAll(paymentCardItems)
        }

        return ImportContent(
            items = items,
            tags = emptyList(),
            unknownItems = unknownItems,
        )
    }

    private fun parsePaymentCards(jsonText: String, vaultId: String): List<Item> {
        return try {
            val model = json.decodeFromString<ApplePaymentCards>(jsonText)
            val items = mutableListOf<Item>()

            model.payment_cards.forEach { card ->
                val cardNumberString = card.card_number?.trim()?.takeIf { it.isNotBlank() }
                val expirationDateString: String? = if (card.card_expiration_month != null && card.card_expiration_year != null) {
                    val yearSuffix = if (card.card_expiration_year > 99) {
                        card.card_expiration_year % 100
                    } else {
                        card.card_expiration_year
                    }
                    String.format("%02d/%02d", card.card_expiration_month, yearSuffix)
                } else {
                    null
                }

                val cardNumber = cardNumberString?.let { SecretField.ClearText(it) }
                val expirationDate = expirationDateString?.let { SecretField.ClearText(it) }
                val cardNumberMask = cardNumberString?.let { detectCardNumberMask(it) }
                val cardIssuer = cardNumberString?.let { detectCardIssuer(it) }

                items.add(
                    Item.create(
                        vaultId = vaultId,
                        contentType = ItemContentType.PaymentCard,
                        content = ItemContent.PaymentCard.Empty.copy(
                            name = card.card_name?.trim()?.takeIf { it.isNotBlank() }.orEmpty(),
                            cardHolder = card.cardholder_name?.trim()?.takeIf { it.isNotBlank() },
                            cardIssuer = cardIssuer,
                            cardNumber = cardNumber,
                            cardNumberMask = cardNumberMask,
                            expirationDate = expirationDate,
                            securityCode = null,
                            notes = null,
                        ),
                    ),
                )
            }

            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Apple PaymentCards JSON Models
    @Serializable
    private data class ApplePaymentCards(
        val payment_cards: List<ApplePaymentCard>,
    )

    @Serializable
    private data class ApplePaymentCard(
        val card_number: String? = null,
        val card_name: String? = null,
        val cardholder_name: String? = null,
        val card_expiration_month: Int? = null,
        val card_expiration_year: Int? = null,
    )

    private fun detectCardNumberMask(cardNumber: String): String? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.length < 4) return null
        return digitsOnly.takeLast(4)
    }

    private fun detectCardIssuer(cardNumber: String): ItemContent.PaymentCard.Issuer? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        return when {
            digitsOnly.startsWith("4") -> ItemContent.PaymentCard.Issuer.Visa
            digitsOnly.startsWith("5") -> ItemContent.PaymentCard.Issuer.MasterCard
            digitsOnly.startsWith("34") || digitsOnly.startsWith("37") -> ItemContent.PaymentCard.Issuer.AmericanExpress
            digitsOnly.startsWith("6011") || digitsOnly.startsWith("65") -> ItemContent.PaymentCard.Issuer.Discover
            digitsOnly.startsWith("35") -> ItemContent.PaymentCard.Issuer.Jcb
            digitsOnly.startsWith("62") -> ItemContent.PaymentCard.Issuer.UnionPay
            else -> null
        }
    }
}