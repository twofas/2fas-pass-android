package com.twofasapp.feature.settings.ui.autofill.browsers

import androidx.compose.ui.graphics.painter.Painter

internal data class BrowserAutofillStatus(
    val name: String,
    val packageName: String,
    val icon: Painter? = null,
    val autofillEnabled: Boolean = false,
) {
    companion object {
        val SupportedBrowsers = listOf(
            BrowserAutofillStatus(
                name = "Chrome",
                packageName = "com.android.chrome",
            ),
            BrowserAutofillStatus(
                name = "Chrome Beta",
                packageName = "com.chrome.beta",
            ),
            BrowserAutofillStatus(
                name = "Brave",
                packageName = "com.brave.browser",
            ),
        )
    }
}