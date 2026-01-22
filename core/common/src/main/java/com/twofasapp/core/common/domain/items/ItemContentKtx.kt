package com.twofasapp.core.common.domain.items

fun String.formatWithGrouping(grouping: List<Int>): String {
    if (isEmpty()) return this

    val result = StringBuilder()
    var position = 0

    for (groupSize in grouping) {
        if (position >= length) break

        if (result.isNotEmpty()) {
            result.append(" ")
        }

        val endPosition = minOf(position + groupSize, length)
        result.append(substring(position, endPosition))
        position = endPosition

        if (position >= length) break
    }

    if (position < length) {
        if (result.isNotEmpty()) {
            result.append(" ")
        }
        result.append(substring(position))
    }

    return result.toString()
}

fun ItemContent.PaymentCard.Issuer?.cardNumberGrouping(): List<Int> {
    return when (this) {
        ItemContent.PaymentCard.Issuer.AmericanExpress -> listOf(4, 6, 5)
        ItemContent.PaymentCard.Issuer.DinersClub -> listOf(4, 6, 4)
        ItemContent.PaymentCard.Issuer.Visa,
        ItemContent.PaymentCard.Issuer.MasterCard,
        ItemContent.PaymentCard.Issuer.Discover,
        ItemContent.PaymentCard.Issuer.Jcb,
        ItemContent.PaymentCard.Issuer.UnionPay,
        null,
        -> listOf(4, 4, 4, 4, 3)
    }
}