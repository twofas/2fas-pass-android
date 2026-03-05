package com.twofasapp.feature.credentialprovider.handler

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo.Builder
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.provider.BiometricPromptResult
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.credentials.webauthn.AuthenticatorAssertionResponse
import androidx.credentials.webauthn.FidoPublicKeyCredential
import androidx.credentials.webauthn.PublicKeyCredentialRequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.security.Signature

@SuppressLint("RestrictedApi")
class PassKeyGetHandler {

    fun handle(
        intent: Intent,
        activity: AppCompatActivity,
        resultCallback: (Intent?) -> Unit
    ) {
        val request =
            PendingIntentHandler.retrieveProviderGetCredentialRequest(intent)

        val option = request?.credentialOptions.orEmpty()
            .filter { option -> option.type == PublicKeyCredential.TYPE_PUBLIC_KEY_CREDENTIAL }
            .filterIsInstance<GetPublicKeyCredentialOption>()
            .firstOrNull()

        when {
            request == null -> resultCallback(null)

            option != null ->
                handle(
                    option,
                    request.callingAppInfo,
                    request.biometricPromptResult,
                    activity,
                    resultCallback
                )

            else -> error(resultCallback)
        }
    }

    private fun error(resultCallback: (Intent) -> Unit) {
        resultCallback(
            Intent().apply {
                PendingIntentHandler.setGetCredentialException(
                    this,
                    GetCredentialUnknownException(),
                )
            }
        )
    }

    private fun handle(
        option: GetPublicKeyCredentialOption,
        callingAppInfo: CallingAppInfo,
        biometricPromptResult: BiometricPromptResult?,
        activity: AppCompatActivity,
        resultCallback: (Intent) -> Unit
    ) {
//        if (biometricPromptResult == null) {
//            checkBiometric(request, callingAppInfo, activity, resultCallback)
//            return
//        }
//
//        if (biometricPromptResult.isSuccessful) {
//            createResponse(request, callingAppInfo, resultCallback)
//            return
//        }
//
//        this@PassKeyCreateHandler.error(resultCallback)
        createResponse(option, callingAppInfo, resultCallback)
    }

    private fun checkBiometric(
        option: GetPublicKeyCredentialOption,
        callingAppInfo: CallingAppInfo,
        activity: AppCompatActivity,
        resultCallback: (Intent) -> Unit
    ) {
        val biometricPrompt = BiometricPrompt(
            activity,
            Dispatchers.IO.asExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    error(resultCallback)
                }

                override fun onAuthenticationFailed() {
                    error(resultCallback)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    createResponse(option, callingAppInfo, resultCallback)
                }
            },
        )
        val promptInfo = Builder()
            .setTitle("Bio")
            .setSubtitle("Bio")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun createResponse(
        option: GetPublicKeyCredentialOption,
        callingAppInfo: CallingAppInfo,
        resultCallback: (Intent) -> Unit
    ) {
        val credentialId = KeySingleton.credentialId ?: ByteArray(0)
        val publicKeyRequestOptions = PublicKeyCredentialRequestOptions(option.requestJson)
        val origin = appInfoToOrigin(callingAppInfo)
        val packageName = callingAppInfo.packageName
        val clientDataHash = option.clientDataHash
        val userHandle = KeySingleton.userHandle ?: ByteArray(0)

        val response = AuthenticatorAssertionResponse(
            requestOptions = publicKeyRequestOptions,
            credentialId = credentialId,
            origin = origin,
            up = true,
            uv = true,
            be = true,
            bs = true,
            userHandle = userHandle,
            packageName = packageName,
            clientDataHash = clientDataHash,
        )

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(KeySingleton.key)
        signature.update(response.dataToSign())
        response.signature = signature.sign()

        val credential = PassFidoPublicKeyCredential(
            FidoPublicKeyCredential(
                rawId = credentialId,
                response = response,
                authenticatorAttachment = "platform",
            )
        )
        val cred = PublicKeyCredential(credential.json())

        resultCallback(
            Intent().apply {
                PendingIntentHandler.setGetCredentialResponse(
                    this,
                    GetCredentialResponse(cred),
                )
            }
        )
    }
}