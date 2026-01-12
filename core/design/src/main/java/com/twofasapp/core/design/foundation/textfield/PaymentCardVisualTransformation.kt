package com.twofasapp.core.design.foundation.textfield

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@Stable
fun VisualTransformation.Companion.PaymentCard(maxLength: Int): VisualTransformation {
    return VisualTransformation { text ->
        val textTrimmed = if (text.text.length >= maxLength) text.text.substring(0 until maxLength) else text.text
        var out = ""

        for (i in textTrimmed.indices) {
            out += textTrimmed[i]
            if (i % 4 == 3 && i != maxLength - 1) out += " "
        }

        val spacesCount = (maxLength - 1) / 4
        val paymentCardOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val spaces = minOf(offset / 4, spacesCount)
                return offset + spaces
            }

            override fun transformedToOriginal(offset: Int): Int {
                val spaces = minOf(offset / 5, spacesCount)
                return offset - spaces
            }
        }

        TransformedText(AnnotatedString(out), paymentCardOffsetTranslator)
    }
}

@Stable
val VisualTransformation.Companion.PaymentCardExpirationDate: VisualTransformation
    get() = VisualTransformation { text ->
        val textTrimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""

        for (i in textTrimmed.indices) {
            out += textTrimmed[i]

            if (i == 1) {
                out += " / "
            }
        }

        val paymentCardOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 3
                return 7
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 7) return offset - 3
                return 4
            }
        }

        TransformedText(AnnotatedString(out), paymentCardOffsetTranslator)
    }