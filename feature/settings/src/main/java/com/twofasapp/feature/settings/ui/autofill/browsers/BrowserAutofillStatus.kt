package com.twofasapp.feature.settings.ui.autofill.browsers

import androidx.compose.ui.graphics.painter.Painter

data class BrowserAutofillStatus(
    val name: String,
    val packageName: String,
    val icon: Painter? = null,
    val autofillEnabled: Boolean = false,
    val alwaysEnabled: Boolean = false,
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
            BrowserAutofillStatus(
                name = "Firefox",
                packageName = "org.mozilla.firefox",
                alwaysEnabled = true,
            ),
            BrowserAutofillStatus(
                name = "Firefox Beta",
                packageName = "org.mozilla.firefox_beta",
                alwaysEnabled = true,
            ),
        )
    }
}