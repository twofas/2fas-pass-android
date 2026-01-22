package com.twofasapp.data.main.mapper

import com.twofasapp.core.common.domain.items.ItemContent

object PaymentCardValidator {

    fun maxCardNumberLength(issuer: ItemContent.PaymentCard.Issuer?): Int {
        return when (issuer) {
            ItemContent.PaymentCard.Issuer.Visa,
            ItemContent.PaymentCard.Issuer.MasterCard,
            ItemContent.PaymentCard.Issuer.Jcb,
            -> 16

            ItemContent.PaymentCard.Issuer.AmericanExpress -> 15

            ItemContent.PaymentCard.Issuer.Discover,
            ItemContent.PaymentCard.Issuer.DinersClub,
            ItemContent.PaymentCard.Issuer.UnionPay,
            -> 19

            null -> 19
        }
    }

    fun minCardNumberLength(issuer: ItemContent.PaymentCard.Issuer?): Int {
        return when (issuer) {
            ItemContent.PaymentCard.Issuer.Visa,
            ItemContent.PaymentCard.Issuer.MasterCard,
            ItemContent.PaymentCard.Issuer.Jcb,
            -> 16

            ItemContent.PaymentCard.Issuer.AmericanExpress -> 15

            ItemContent.PaymentCard.Issuer.DinersClub -> 14

            ItemContent.PaymentCard.Issuer.Discover,
            ItemContent.PaymentCard.Issuer.UnionPay,
            -> 16

            null -> 13
        }
    }

    fun maxSecurityCodeLength(issuer: ItemContent.PaymentCard.Issuer?): Int {
        return when (issuer) {
            ItemContent.PaymentCard.Issuer.AmericanExpress,
            null,
            -> 4

            ItemContent.PaymentCard.Issuer.Visa,
            ItemContent.PaymentCard.Issuer.MasterCard,
            ItemContent.PaymentCard.Issuer.Discover,
            ItemContent.PaymentCard.Issuer.DinersClub,
            ItemContent.PaymentCard.Issuer.Jcb,
            ItemContent.PaymentCard.Issuer.UnionPay,
            -> 3
        }
    }

    fun detectCardIssuer(cardNumber: String?): ItemContent.PaymentCard.Issuer? {
        if (cardNumber.isNullOrBlank()) return null

        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        // Visa: starts with 4
        if (digitsOnly.startsWith("4")) {
            return ItemContent.PaymentCard.Issuer.Visa
        }

        // Mastercard: starts with 51-55 or 2221-2720
        if (digitsOnly.length >= 2) {
            val first2 = digitsOnly.take(2).toIntOrNull()
            if (first2 != null && first2 in 51..55) {
                return ItemContent.PaymentCard.Issuer.MasterCard
            }
        }
        if (digitsOnly.length >= 4) {
            val first4 = digitsOnly.take(4).toIntOrNull()
            if (first4 != null && first4 in 2221..2720) {
                return ItemContent.PaymentCard.Issuer.MasterCard
            }
        }

        // American Express: starts with 34 or 37
        if (digitsOnly.startsWith("34") || digitsOnly.startsWith("37")) {
            return ItemContent.PaymentCard.Issuer.AmericanExpress
        }

        // Discover: starts with 6011, 622126-622925, 644-649, or 65
        if (digitsOnly.startsWith("6011") || digitsOnly.startsWith("65")) {
            return ItemContent.PaymentCard.Issuer.Discover
        }
        if (digitsOnly.length >= 3) {
            val first3 = digitsOnly.take(3).toIntOrNull()
            if (first3 != null && first3 in 644..649) {
                return ItemContent.PaymentCard.Issuer.Discover
            }
        }
        if (digitsOnly.length >= 6) {
            val first6 = digitsOnly.take(6).toIntOrNull()
            if (first6 != null && first6 in 622126..622925) {
                return ItemContent.PaymentCard.Issuer.Discover
            }
        }

        // Diners Club: starts with 300-305, 36, 38-39
        if (digitsOnly.startsWith("36") || digitsOnly.startsWith("38") || digitsOnly.startsWith("39")) {
            return ItemContent.PaymentCard.Issuer.DinersClub
        }
        if (digitsOnly.length >= 3) {
            val first3 = digitsOnly.take(3).toIntOrNull()
            if (first3 != null && first3 in 300..305) {
                return ItemContent.PaymentCard.Issuer.DinersClub
            }
        }

        // JCB: starts with 3528-3589
        if (digitsOnly.length >= 4) {
            val first4 = digitsOnly.take(4).toIntOrNull()
            if (first4 != null && first4 in 3528..3589) {
                return ItemContent.PaymentCard.Issuer.Jcb
            }
        }

        // UnionPay: starts with 62
        if (digitsOnly.startsWith("62")) {
            return ItemContent.PaymentCard.Issuer.UnionPay
        }

        return null
    }

    fun cardNumberMask(cardNumber: String?): String? {
        if (cardNumber.isNullOrBlank()) return null

        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null

        return digitsOnly.takeLast(4)
    }

    fun validateExpirationDate(value: String): Boolean {
        val components = value.split("/")
        if (components.size != 2) return false

        val monthStr = components[0]
        val yearStr = components[1]

        if (monthStr.length != 2 || yearStr.length != 2) return false

        val month = monthStr.toIntOrNull() ?: return false

        return month in 1..12
    }

    fun validateSecurityCode(value: String, issuer: ItemContent.PaymentCard.Issuer?): Boolean {
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.length != value.length) return false

        val expectedLength = maxSecurityCodeLength(issuer)
        return if (issuer != null) {
            value.length == expectedLength
        } else {
            value.length in 3..4
        }
    }

    fun validateCardNumber(value: String, issuer: ItemContent.PaymentCard.Issuer?): Boolean {
        if (!value.all { it.isDigit() || it.isWhitespace() }) return false

        val digitsOnly = value.filter { it.isDigit() }
        val minLength = minCardNumberLength(issuer)
        val maxLength = maxCardNumberLength(issuer)

        if (digitsOnly.length !in minLength..maxLength) return false

        return luhnCheck(digitsOnly)
    }

    private fun luhnCheck(cardNumber: String): Boolean {
        val digits = cardNumber.mapNotNull { it.digitToIntOrNull() }
        if (digits.size != cardNumber.length) return false

        var sum = 0
        digits.reversed().forEachIndexed { index, digit ->
            if (index % 2 == 1) {
                val doubled = digit * 2
                sum += if (doubled > 9) doubled - 9 else doubled
            } else {
                sum += digit
            }
        }

        return sum % 10 == 0
    }
}