/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.authentication

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.statusBarHeight
import com.twofasapp.core.android.viewmodel.ProvidesViewModelStoreOwner
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.lock.ui.composables.AuthenticationForm
import com.twofasapp.feature.lock.ui.composables.BiometricsModal
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthenticationPrompt(
    title: String,
    description: String,
    cta: String,
    icon: Painter = MdtIcons.Encrypted,
    biometricsAllowed: Boolean,
    animateEnter: Boolean = true,
    onAuthenticated: (masterKey: ByteArray) -> Unit = {},
    onClose: () -> Unit,
) {
    ProvidesViewModelStoreOwner {
        AuthenticationPromptContent(
            title = title,
            description = description,
            cta = cta,
            icon = icon,
            biometricsAllowed = biometricsAllowed,
            animateEnter = animateEnter,
            onClose = onClose,
            onAuthenticated = onAuthenticated,
        )
    }
}

@Composable
private fun AuthenticationPromptContent(
    viewModel: AuthenticationPromptViewModel = koinViewModel(),
    title: String,
    description: String,
    cta: String,
    icon: Painter = MdtIcons.Encrypted,
    biometricsAllowed: Boolean,
    animateEnter: Boolean,
    onAuthenticated: (masterKey: ByteArray) -> Unit = {},
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val strings = MdtLocale.strings
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.initialising) {
        return
    }

    var biometricsDismissed by remember { mutableStateOf(false) }
    var fullPageAuthenticationVisible by remember { mutableStateOf(false) }

    if (uiState.biometricsEnabled && uiState.masterKeyEncryptedWithBiometrics != null && biometricsAllowed && biometricsDismissed.not()) {
        BiometricsModal(
            title = strings.lockScreenBiometricsModalTitle,
            subtitle = title,
            negative = strings.lockUsePassword,
            encryptedBytes = uiState.masterKeyEncryptedWithBiometrics!!,
            onSuccessDecrypt = { masterKeyDecrypted ->
                onAuthenticated(masterKeyDecrypted)
            },
            onDismissRequest = { onClose() },
            onNegativedClick = { biometricsDismissed = true },
            onBiometricsInvalidated = {},
        )
    } else {
        LaunchedEffect(Unit) {
            awaitFrame()
            fullPageAuthenticationVisible = true
        }

        BackHandler {
            scope.launch {
                fullPageAuthenticationVisible = false
                delay(250)
                onClose()
            }
        }

        AnimatedVisibility(
            visible = fullPageAuthenticationVisible,
            enter = if (animateEnter) {
                slideInVertically(tween(250)) { it / 2 }
            } else {
                fadeIn(tween(50))
            },
            exit = slideOutVertically(tween(250)) { it },
            modifier = Modifier.fillMaxSize(),
        ) {
            FullPageAuthenticationContent(
                uiState = uiState,
                title = title,
                description = description,
                cta = cta,
                icon = icon,
                onCheckPassword = {
                    viewModel.checkPassword(it) { masterKey ->
                        scope.launch {
                            fullPageAuthenticationVisible = false
                            delay(250)
                            onAuthenticated(masterKey)
                        }
                    }
                },
                onAuthenticated = { masterKey ->
                    scope.launch {
                        fullPageAuthenticationVisible = false
                        delay(250)
                        onAuthenticated(masterKey)
                    }
                },
                onClose = {
                    scope.launch {
                        fullPageAuthenticationVisible = false
                        delay(250)
                        onClose()
                    }
                },
            )
        }
    }
}

@Composable
private fun FullPageAuthenticationContent(
    uiState: AuthenticationPromptUiState,
    title: String,
    description: String,
    cta: String,
    icon: Painter = MdtIcons.Encrypted,
    onCheckPassword: (password: String) -> Unit = {},
    onAuthenticated: (masterKey: ByteArray) -> Unit = {},
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeight)
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.16f)))),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MdtTheme.color.transparent)
            .statusBarsPadding()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
    ) {
        TopAppBar(
            content = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IconButton(
                        icon = MdtIcons.Close,
                        iconTint = MdtTheme.color.onBackground,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterEnd),
                        onClick = onClose,
                    )
                }
            },
            showBackButton = false,
        )

        AuthenticationForm(
            modifier = Modifier.fillMaxSize(),
            title = title,
            description = description,
            cta = cta,
            icon = icon,
            biometricsEnabled = false,
            masterKeyEncryptedWithBiometrics = null,
            passwordError = uiState.passwordError,
            loading = uiState.loading,
            showBiometricsOnStart = false,
            onUnlockClick = { onCheckPassword(it) },
            onMasterKeyDecrypted = onAuthenticated,
            onBiometricsInvalidated = {},
        )
    }
}