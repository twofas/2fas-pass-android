package com.twofasapp.core.design.foundation.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

fun secretString(count: Int = 12): String {
    return "•".repeat(count)
}

fun secretAnnotatedString(count: Int = 12): AnnotatedString {
    return buildAnnotatedString { append("•".repeat(count)) }
}