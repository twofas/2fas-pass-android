package com.twofasapp.feature.credentialprovider.service

import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.CredentialProviderService
import androidx.credentials.provider.ProviderClearCredentialStateRequest
import com.twofasapp.feature.credentialprovider.handler.PassKeyBeginCreateHandler
import com.twofasapp.feature.credentialprovider.handler.PassKeyBeginGetHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class PassCredentialProviderService : CredentialProviderService(), KoinComponent {

    private val passKeyCreateHandler by inject<PassKeyBeginCreateHandler>()
    private val passKeyGetHandler by inject<PassKeyBeginGetHandler>()

    override fun onBeginCreateCredentialRequest(
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>
    ) {
        val handled = passKeyCreateHandler.handle(request, callback, applicationContext)
        if (handled.not()) {
            callback.onError(CreateCredentialUnknownException())
        }
    }

    override fun onBeginGetCredentialRequest(
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>
    ) {
        val handled = passKeyGetHandler.handle(request, callback, applicationContext)
        if (handled.not()) {
            callback.onError(GetCredentialUnknownException())
        }
    }

    override fun onClearCredentialStateRequest(
        request: ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>
    ) {
        callback.onResult(null)
    }
}