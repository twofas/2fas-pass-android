/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.lock.ui.composables

import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.toastLong
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.cipherDecrypt
import com.twofasapp.core.common.crypto.cipherEncrypt
import com.twofasapp.core.common.crypto.decrypt
import com.twofasapp.core.common.crypto.encrypt
import com.twofasapp.core.common.domain.crypto.CipherMode
import com.twofasapp.core.common.domain.crypto.EncryptedBytes
import com.twofasapp.core.locale.MdtLocale
import org.koin.compose.koinInject

@Composable
fun BiometricsModal(
    title: String,
    subtitle: String,
    negative: String? = null,
    encryptedBytes: EncryptedBytes,
    onSuccessDecrypt: (ByteArray) -> Unit = {},
    onDismissRequest: () -> Unit,
    onNegativedClick: () -> Unit,
    onError: (Int, String) -> Unit = { _, _ -> },
    onBiometricsInvalidated: () -> Unit = {},
) {
    Modal(
        title = title,
        subtitle = subtitle,
        negative = negative,
        cipherMode = CipherMode.Decrypt,
        encryptedBytes = encryptedBytes,
        onSuccessDecrypt = onSuccessDecrypt,
        onDismissRequest = onDismissRequest,
        onNegativedClick = onNegativedClick,
        onError = onError,
        onBiometricsInvalidated = onBiometricsInvalidated,
    )
}

@Composable
fun BiometricsModal(
    title: String,
    subtitle: String,
    negative: String? = null,
    decryptedBytes: ByteArray,
    onSuccessEncrypt: (EncryptedBytes) -> Unit = {},
    onDismissRequest: () -> Unit,
    onNegativedClick: () -> Unit,
    onError: (Int, String) -> Unit = { _, _ -> },
) {
    Modal(
        title = title,
        subtitle = subtitle,
        negative = negative,
        cipherMode = CipherMode.Encrypt,
        decryptedBytes = decryptedBytes,
        onSuccessEncrypt = onSuccessEncrypt,
        onDismissRequest = onDismissRequest,
        onNegativedClick = onNegativedClick,
        onError = onError,
    )
}

@Composable
private fun Modal(
    androidKeyStore: AndroidKeyStore = koinInject(),
    title: String,
    subtitle: String,
    negative: String? = null,
    cipherMode: CipherMode,
    encryptedBytes: EncryptedBytes? = null,
    decryptedBytes: ByteArray? = null,
    onSuccessEncrypt: (EncryptedBytes) -> Unit = {},
    onSuccessDecrypt: (ByteArray) -> Unit = {},
    onDismissRequest: () -> Unit,
    onNegativedClick: () -> Unit,
    onError: (Int, String) -> Unit = { _, _ -> },
    onBiometricsInvalidated: () -> Unit = {},
) {
    when (cipherMode) {
        CipherMode.Encrypt -> requireNotNull(decryptedBytes)
        CipherMode.Decrypt -> requireNotNull(encryptedBytes)
    }

    val context = LocalContext.current
    val strings = MdtLocale.strings
    val activity = LocalContext.currentActivity as? FragmentActivity

    if (activity == null) {
        context.toastLong(strings.biometricsMissingActivityError)
        onDismissRequest()
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)

    val promptInfo = PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negative ?: strings.commonCancel)
        .build()

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            result.cryptoObject?.cipher?.let { cipher ->
                when (cipherMode) {
                    CipherMode.Encrypt -> {
                        onSuccessEncrypt(cipher.encrypt(decryptedBytes!!))
                    }

                    CipherMode.Decrypt -> {
                        onSuccessDecrypt(cipher.decrypt(encryptedBytes!!))
                    }
                }
            } ?: onDismissRequest()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            // Failed eg. due to wrong fingerprint
//            onDismissRequest()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            when (errorCode) {
                BiometricPrompt.ERROR_USER_CANCELED -> onDismissRequest()
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onNegativedClick()
                else -> onError(errorCode, errString.toString())
            }
        }
    }

    val prompt = BiometricPrompt(activity, executor, callback)

    LaunchedEffect(Unit) {
        try {
            val cipher = when (cipherMode) {
                CipherMode.Encrypt -> cipherEncrypt(androidKeyStore.biometricsKey)
                CipherMode.Decrypt -> cipherDecrypt(androidKeyStore.biometricsKey, encryptedBytes!!.iv)
            }

            prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: KeyPermanentlyInvalidatedException) {
            when (cipherMode) {
                CipherMode.Encrypt -> {
                    // Delete current key and re-prompt
                    androidKeyStore.deleteBiometricsKey()
                    prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipherEncrypt(androidKeyStore.biometricsKey)))
                }

                CipherMode.Decrypt -> {
                    onBiometricsInvalidated()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context.toastLong(strings.biometricsGenericError.format(e.message ?: ""))
            onDismissRequest()
        }
    }
}