/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.twofasapp.core.common.crypto.AndroidKeyStore
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyGenerator

internal class AndroidKeyStoreImpl : AndroidKeyStore {
    companion object {
        private const val keyStoreProvider = "AndroidKeyStore"
        private const val keyPurposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        private const val keyAlgorithm = KeyProperties.KEY_ALGORITHM_AES
        private const val keyBlockMode = KeyProperties.BLOCK_MODE_GCM
        private const val keyPadding = KeyProperties.ENCRYPTION_PADDING_NONE

        private const val appKeyAlias = "twofasapp_app_key"
        private const val biometricsKeyAlias = "twofasapp_biometrics_key"
        private const val connectPersistentKeyAlias = "twofasapp_connect_persistent_key"
        private const val connectEphemeralKeyAlias = "twofasapp_connect_ephemeral_key"
    }

    private val keyStore: KeyStore
        get() = KeyStore.getInstance(keyStoreProvider).also { it.load(null) }

    override val appKey: Key
        get() {
            if (keyStore.containsAlias(appKeyAlias)) {
                return keyStore.getKey(appKeyAlias, null)
            }

            return KeyGenerator.getInstance(keyAlgorithm, keyStoreProvider).run {
                init(
                    KeyGenParameterSpec
                        .Builder(appKeyAlias, keyPurposes)
                        .setBlockModes(keyBlockMode)
                        .setEncryptionPaddings(keyPadding)
                        .setKeySize(256)
                        .build(),
                )

                generateKey()
            }
        }

    override val biometricsKey: Key
        get() {
            if (keyStore.containsAlias(biometricsKeyAlias)) {
                return keyStore.getKey(biometricsKeyAlias, null)
            }

            return KeyGenerator.getInstance(keyAlgorithm, keyStoreProvider).run {
                init(
                    KeyGenParameterSpec
                        .Builder(biometricsKeyAlias, keyPurposes)
                        .setBlockModes(keyBlockMode)
                        .setEncryptionPaddings(keyPadding)
                        .setKeySize(256)
                        .setUserAuthenticationRequired(true)
                        .setInvalidatedByBiometricEnrollment(true)
                        .build(),
                )

                generateKey()
            }
        }

    override val connectPersistentEcKey: KeyPair
        get() {
            if (keyStore.containsAlias(connectPersistentKeyAlias)) {
                return KeyPair(
                    /* publicKey = */
                    keyStore.getCertificate(connectPersistentKeyAlias)?.publicKey,
                    /* privateKey = */
                    keyStore.getKey(connectPersistentKeyAlias, null) as? PrivateKey,
                )
            }

            return generateEcKey(connectPersistentKeyAlias)
        }

    override fun generateConnectEphemeralEcKey(): KeyPair {
        return generateEcKey(connectEphemeralKeyAlias)
    }

    override fun deleteBiometricsKey() {
        keyStore.deleteEntry(biometricsKeyAlias)
    }

    private fun generateEcKey(alias: String): KeyPair {
        return KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, keyStoreProvider)
            .run {
                initialize(
                    KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_AGREE_KEY)
                        .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setUserAuthenticationRequired(false)
                        .build(),
                )

                generateKeyPair()
            }
    }
}