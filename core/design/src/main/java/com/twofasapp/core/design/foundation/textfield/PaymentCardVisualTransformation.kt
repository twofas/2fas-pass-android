package com.twofasapp.core.design.foundation.textfield

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.twofasapp.core.common.domain.items.formatWithGrouping

@Stable
fun VisualTransformation.Companion.PaymentCard(grouping: List<Int>): VisualTransformation {
    return VisualTransformation { text ->
        val textTrimmed = text.text
        val out = textTrimmed.formatWithGrouping(grouping)

        // Calculate cumulative group positions for offset mapping
        val cumulativeGroupSizes = buildList {
            var sum = 0
            for (size in grouping) {
                sum += size
                add(sum)
            }
        }

        val paymentCardOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Count how many spaces should be added before this offset
                var spaces = 0
                for (groupEnd in cumulativeGroupSizes) {
                    if (offset > groupEnd) {
                        spaces++
                    } else {
                        break
                    }
                }
                return offset + spaces
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Count how many spaces are in the string before this offset
                var spaces = 0
                for (groupEnd in cumulativeGroupSizes) {
                    val transformedGroupEnd = groupEnd + spaces
                    if (offset > transformedGroupEnd) {
                        spaces++
                    } else {
                        break
                    }
                }
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