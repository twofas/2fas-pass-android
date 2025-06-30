/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.getSafelyParcelable
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.autofill.service.builders.IntentBuilders.EXTRA_LOGIN
import com.twofasapp.feature.autofill.service.builders.IntentBuilders.replyWithSuccess
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.lock.ui.authentication.AuthenticationPrompt
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AutofillAuthScreen(
    viewModel: AutofillAuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.currentActivity
    val login = activity.intent.extras.getSafelyParcelable<AutofillLogin>(EXTRA_LOGIN)

    LaunchedEffect(Unit) {
        viewModel.initLogin(login)
    }

    Content(
        onMasterKeyDecrypted = {
            viewModel.authenticate(it) { autofillLogin ->
                activity.replyWithSuccess(autofillLogin)
            }
        },
        onCloseClick = { activity.finishAndRemoveTask() },
    )
}

@Composable
private fun Content(
    onMasterKeyDecrypted: (ByteArray) -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    AuthenticationPrompt(
        title = MdtLocale.strings.autofillPromptTitle,
        description = MdtLocale.strings.autofillPromptDescription,
        cta = MdtLocale.strings.autofillPromptCta,
        icon = MdtIcons.LockKeyboard,
        biometricsAllowed = true,
        animateEnter = false,
        onAuthenticated = { onMasterKeyDecrypted(it) },
        onClose = onCloseClick,
    )
}

@Preview
@Composable
private fun Previews() {
    PreviewTheme {
        Content()
    }
}