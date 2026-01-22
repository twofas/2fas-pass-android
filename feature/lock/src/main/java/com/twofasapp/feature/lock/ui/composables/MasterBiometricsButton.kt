/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.common.domain.crypto.emptyEncryptedBytes
import com.twofasapp.core.design.foundation.button.TextButton
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import kotlinx.coroutines.android.awaitFrame

@Composable
fun MasterBiometricsButton(
    modifier: Modifier = Modifier,
    text: String,
    biometricsEnabled: Boolean,
    masterKey: EncryptedBytes?,
    modalTitle: String? = null,
    modalSubtitle: String? = null,
    showOnStart: Boolean = true,
    onMasterKeyDecrypted: (ByteArray) -> Unit = {},
    onBiometricsInvalidated: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    val resolvedModalTitle = modalTitle ?: strings.lockScreenBiometricsModalTitle
    val resolvedModalSubtitle = modalSubtitle ?: strings.lockScreenBiometricsModalSubtitle
    var showBiometricsModal by remember { mutableStateOf(false) }

    if (biometricsEnabled && masterKey != null) {
        TextButton(
            modifier = modifier,
            text = text,
            onClick = { showBiometricsModal = true },
        )
    }

    LaunchedEffect(biometricsEnabled) {
        awaitFrame()
        if (biometricsEnabled && showOnStart) {
            showBiometricsModal = true
        }
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            showBiometricsModal = false
        }
    }

    if (showBiometricsModal && masterKey != null) {
        BiometricsModal(
            title = resolvedModalTitle,
            subtitle = resolvedModalSubtitle,
            negative = strings.lockUsePassword,
            encryptedBytes = masterKey,
            onSuccessDecrypt = { masterKeyDecrypted ->
                showBiometricsModal = false
                onMasterKeyDecrypted(masterKeyDecrypted)
            },
            onDismissRequest = { showBiometricsModal = false },
            onNegativedClick = { showBiometricsModal = false },
            onBiometricsInvalidated = {
                showBiometricsModal = false
                onBiometricsInvalidated()
            },
        )
    }
}

@Preview
@Composable
private fun Previews() {
    PreviewTheme {
        MasterBiometricsButton(
            modifier = Modifier,
            text = MdtLocale.strings.authUseBiometrics,
            biometricsEnabled = true,
            masterKey = emptyEncryptedBytes(),
        )
    }
}