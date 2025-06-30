/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.startup.ui.createdecryptionkit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.clearTmpDir
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.decryptionkit.ui.DecryptionKitScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CreateDecryptionKitScreen(
    viewModel: CreateDecryptionKitViewModel = koinViewModel(),
    openNextStep: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    DecryptionKitScreen(
        decryptionKit = uiState.decryptionKit,
        screenHeaderTitle = MdtLocale.strings.decryptionKitTitle,
        screenHeaderDescription = MdtLocale.strings.decryptionKitDescription,
        screenHeaderImage = painterResource(com.twofasapp.feature.startup.R.drawable.progress_shield_75),
        requireSaveConfirmation = true,
        onComplete = {
            scope.launch {
                context.clearTmpDir()
                openNextStep()
            }
        },
    )
}