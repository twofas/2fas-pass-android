/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.parser

import android.text.InputType
import android.view.View

internal sealed interface AutofillInputConfig {
    val autofillHints: Set<String>
    val inputTypeFlags: Set<Int>
    val regex: Regex
    val keywords: Set<String>
    val deniedKeywords: Set<String>
    val htmlAttributes: Set<String>

    data object Username : AutofillInputConfig {
        override val autofillHints = setOf(
            View.AUTOFILL_HINT_EMAIL_ADDRESS,
            View.AUTOFILL_HINT_USERNAME,
        )

        override val inputTypeFlags = setOf(
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
        )

        override val regex = Regex(
            pattern = "(?:log(?:in|ging)|sign(?:in|on|up)|auth(?:enticate|entication)|user(?:name)?|e?mail|connect|access|account)",
            option = RegexOption.IGNORE_CASE,
        )

        override val keywords = setOf(
            "account",
            "accountid",
            "accountname",
            "accountnametextfield",
            "authemail",
            "authemailid",
            "authlogin",
            "authuser",
            "email",
            "emailid",
            "emailaddress",
            "elogin",
            "eloginid",
            "eloginuser",
            "eloginname",
            "eloginusername",
            "logonid",
            "login",
            "loginemail",
            "loginemailid",
            "loginid",
            "logininput",
            "loginuser",
            "loginuserid",
            "loginusername",
            "loginname",
            "logininputemail",
            "logininputusername",
            "loginuserinput",
            "signinemail",
            "signinemailid",
            "signinlogin",
            "signinuser",
            "signupemail",
            "signupemailid",
            "signuplogin",
            "signupuser",
            "registeremail",
            "registeremailid",
            "registerlogin",
            "registeruser",
            "user",
            "userid",
            "useremail",
            "useremailid",
            "userlogin",
            "userlogon",
            "userloginid",
            "username",
            "useremaillogin",
            "userloginemail",
            "userlogininput",
            "widgetemail",
            "widgetlogin",
            "widgetuser",
            "widgetuseremail",
        )

        override val deniedKeywords = setOf(
            "password",
            "pass",
            "pwd",
            "newsletter",
            "search",
            "find",
            "subscribe",
        )

        override val htmlAttributes = setOf(
            "email",
            "username",
        )
    }

    data object Password : AutofillInputConfig {
        override val autofillHints = setOf(
            View.AUTOFILL_HINT_PASSWORD,
        )

        override val inputTypeFlags = setOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        )

        override val regex = Regex(
            pattern = "(?:pass(?:word|code|phrase)?|psw|pwd)",
            option = RegexOption.IGNORE_CASE,
        )

        override val keywords = setOf(
            "accountpass",
            "accountpassword",
            "accountpwd",
            "authpass",
            "authpassword",
            "authpwd",
            "logininputpass",
            "logininputpassword",
            "logininputpwd",
            "loginpass",
            "loginpassword",
            "loginpwd",
            "pass",
            "passcode",
            "passkey",
            "passwd",
            "password",
            "passwordinput",
            "passwordtext",
            "pwd",
            "pwdinput",
            "registerpass",
            "registerpassword",
            "registerpwd",
            "securitypass",
            "securitypassword",
            "securitypwd",
            "signinpass",
            "signinpassword",
            "signinpwd",
            "signuppass",
            "signuppassword",
            "signuppwd",
            "userpass",
            "userpassword",
            "userpwd",
            "widgetpass",
            "widgetpassword",
            "widgetpwd",
        )

        override val deniedKeywords = setOf(
            "email",
            "newsletter",
            "search",
            "find",
            "subscribe",
        )

        override val htmlAttributes = setOf(
            "password",
        )
    }
}