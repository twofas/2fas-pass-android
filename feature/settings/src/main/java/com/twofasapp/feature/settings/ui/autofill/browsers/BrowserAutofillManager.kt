package com.twofasapp.feature.settings.ui.autofill.browsers

internal interface BrowserAutofillManager {
    fun checkBrowsersStatus(): List<BrowserAutofillStatus>
}