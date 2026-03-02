package com.twofasapp.feature.credentialprovider.handler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.os.OutcomeReceiver
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BiometricPromptData
import androidx.credentials.provider.CreateEntry
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.feature.credentialprovider.ui.PassCredentialProviderActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class PassKeyBeginCreateHandler(private val vaultsRepository: VaultsRepository) {

    private val requestCode = AtomicInt(0)

    fun handle(
        request: BeginCreateCredentialRequest,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>,
        context: Context,
    ): Boolean {
        if (request.type != PublicKeyCredential.TYPE_PUBLIC_KEY_CREDENTIAL) {
            return false
        }

        GlobalScope.launch {
            callback.onResult(
                BeginCreateCredentialResponse.Builder()
                    .addCreateEntry(
                        CreateEntry.Builder(
                            vaultsRepository.getVault().id,
                            PendingIntent.getActivity(
                                context,
                                requestCode.fetchAndAdd(1),
                                Intent(
                                    context,
                                    PassCredentialProviderActivity::class.java
                                ),
                                (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
                            )
                        )
                            .apply {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                                    setBiometricPromptData(
                                        BiometricPromptData(
                                            cryptoObject = null,
                                            allowedAuthenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                                        ),
                                    )
                                }
                            }
                            .build()
                    )
                    .build()
            )
        }

        return true
    }
}