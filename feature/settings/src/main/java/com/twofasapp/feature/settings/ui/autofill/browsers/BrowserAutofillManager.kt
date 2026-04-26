package com.twofasapp.feature.settings.ui.autofill.browsers

interface BrowserAutofillManager {
    fun checkBrowsersStatus(): List<BrowserAutofillStatus>
}