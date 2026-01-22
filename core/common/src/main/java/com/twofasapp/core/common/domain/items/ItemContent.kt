package com.twofasapp.core.common.domain.items

import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.UriMatcher
import com.twofasapp.core.common.domain.clearTextOrNull

sealed interface ItemContent {
    val name: String

    val defaultLabelText: String
        get() = name.trim().take(2).uppercase()

    data class Unknown private constructor(
        override val name: String,
        val rawJson: String,
    ) : ItemContent {
        constructor(rawJson: String) : this(name = "", rawJson = rawJson)
    }

    data class Login(
        override val name: String,
        val username: String?,
        val password: SecretField?,
        val uris: List<ItemUri>,
        val iconType: IconType,
        val iconUriIndex: Int?,
        val customImageUrl: String?,
        val labelText: String?,
        val labelColor: String?,
        val notes: String?,
    ) : ItemContent {

        companion object {
            val Empty = Login(
                name = "",
                username = null,
                password = null,
                uris = emptyList(),
                iconType = IconType.Icon,
                iconUriIndex = 0,
                customImageUrl = null,
                labelText = null,
                labelColor = null,
                notes = null,
            )

            fun create(
                name: String? = null,
                username: String? = null,
                password: String? = null,
                url: String? = null,
                notes: String? = null,
            ): Login {
                val itemUri = url?.let { url ->
                    ItemUri(
                        text = url,
                        matcher = UriMatcher.Domain,
                    )
                }

                return Login(
                    name = name.orEmpty(),
                    username = username,
                    password = password?.let { SecretField.ClearText(it) },
                    iconType = IconType.Icon,
                    iconUriIndex = if (itemUri == null) null else 0,
                    uris = listOfNotNull(itemUri),
                    customImageUrl = null,
                    labelText = null,
                    labelColor = null,
                    notes = notes,
                )
            }
        }

        val iconUrl: String?
            get() = iconUriIndex?.let { uris.getOrNull(it)?.iconUrl }
    }

    data class SecureNote(
        override val name: String,
        val text: SecretField?,
        val additionalInfo: String?,
    ) : ItemContent {
        companion object {
            val Empty = SecureNote(
                name = "",
                text = null,
                additionalInfo = null,
            )

            fun create(
                name: String? = null,
                text: String? = null,
            ): SecureNote {
                return SecureNote(
                    name = name.orEmpty(),
                    text = text?.let { SecretField.ClearText(it) },
                    additionalInfo = null,
                )
            }

            val Limit: Int = 16_384
        }
    }

    data class PaymentCard(
        override val name: String,
        val cardHolder: String?,
        val cardNumber: SecretField?,
        val cardNumberMask: String?,
        val expirationDate: SecretField?,
        val securityCode: SecretField?,
        val cardIssuer: Issuer?,
        val notes: String?,
    ) : ItemContent {

        val cardNumberMaskDisplay: String
            get() {
                if (cardNumberMask == null) return ""

                val expectedLength = cardNumber.clearTextOrNull?.length ?: cardIssuer?.cardLength ?: 16
                val dotsCount = maxOf(0, expectedLength - 4)
                val dots = "•".repeat(dotsCount)
                val fullMasked = dots + cardNumberMask

                return fullMasked.formatWithGrouping(cardIssuer.cardNumberGrouping())
            }

        val cardNumberMaskDisplayShort: String
            get() {
                if (cardNumberMask == null) return ""
                val dots = "•".repeat(4)
                return "$dots $cardNumberMask".trim()
            }

        enum class Issuer(val code: String, val cardLength: Int) {
            Visa("Visa", 16),
            MasterCard("MC", 16),
            AmericanExpress("AMEX", 15),
            Discover("Discover", 19),
            DinersClub("DinersClub", 14),
            Jcb("JCB", 16),
            UnionPay("UnionPay", 19),
            ;

            companion object {
                fun fromCode(code: String?): Issuer? {
                    return entries.find { it.code == code }
                }
            }
        }

        companion object {
            val Empty = PaymentCard(
                name = "",
                cardHolder = null,
                cardNumber = null,
                cardNumberMask = null,
                expirationDate = null,
                securityCode = null,
                cardIssuer = null,
                notes = null,
            )
        }
    }
}