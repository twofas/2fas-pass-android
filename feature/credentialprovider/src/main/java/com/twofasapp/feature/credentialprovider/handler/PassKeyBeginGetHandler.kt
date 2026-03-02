package com.twofasapp.feature.credentialprovider.handler

import android.content.Context
import android.os.OutcomeReceiver
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class PassKeyBeginGetHandler {

    private val requestCode = AtomicInt(0)

    fun handle(
        request: BeginGetCredentialRequest,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>,
        context: Context
    ): Boolean {
        val options = request.beginGetCredentialOptions
            .filter { option -> option.type == PublicKeyCredential.TYPE_PUBLIC_KEY_CREDENTIAL }
            .filterIsInstance<BeginGetPublicKeyCredentialOption>()

        if (options.isEmpty()) {
            return false
        }

        callback.onResult(
            BeginGetCredentialResponse.Builder()
//                .addAction(
//                    Action.Builder(
//                        "Test",
//                        PendingIntent.getActivity(
//                            context,
//                            requestCode.fetchAndAdd(1),
//                            Intent(
//                                context,
//                                PassCredentialProviderActivity::class.java
//                            ),
//                            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
//                        )
//                    )
//                        .build()
//                )
//                .addAuthenticationAction(
//                    AuthenticationAction.Builder(
//                        "addAuthenticationAction",
//                        PendingIntent.getActivity(
//                            context,
//                            requestCode.fetchAndAdd(1),
//                            Intent(
//                                context,
//                                PassCredentialProviderActivity::class.java
//                            ),
//                            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
//                        )
//                    )
//                        .build()
//                )
//                .addCredentialEntry(
//                    PublicKeyCredentialEntry.Builder(
//                        context,
//                        "Test",
//                        PendingIntent.getActivity(
//                            context,
//                            requestCode.fetchAndAdd(1),
//                            Intent(
//                                context,
//                                PassCredentialProviderActivity::class.java
//                            ),
//                            (PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
//                        ),
//                        BeginGetPublicKeyCredentialOption(Bundle(),"Emp",options.)
//                    )
//                        .build()
//                )
                .build()
        )
        return true
    }
}