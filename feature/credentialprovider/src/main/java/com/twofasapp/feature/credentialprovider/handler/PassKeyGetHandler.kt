package com.twofasapp.feature.credentialprovider.handler

import android.annotation.SuppressLint
import android.content.Intent
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.credentials.webauthn.AuthenticatorAssertionResponse
import androidx.credentials.webauthn.FidoPublicKeyCredential
import androidx.credentials.webauthn.PublicKeyCredentialRequestOptions
import com.twofasapp.core.common.domain.clearText
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.data.main.ItemsRepository
import java.security.Signature

@SuppressLint("RestrictedApi")
class PassKeyGetHandler(private val itemsRepository: ItemsRepository) {

    suspend fun handle(
        intent: Intent,
        resultCallback: suspend (Intent?) -> Unit
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
                    resultCallback
                )

            else -> error(resultCallback)
        }
    }

    private suspend fun error(resultCallback: suspend (Intent) -> Unit) {
        resultCallback(
            Intent().apply {
                PendingIntentHandler.setGetCredentialException(
                    this,
                    GetCredentialUnknownException(),
                )
            }
        )
    }

    suspend fun handle(
        option: GetPublicKeyCredentialOption,
        callingAppInfo: CallingAppInfo,
        resultCallback: suspend (Intent) -> Unit
    ) {
        createResponse(option, callingAppInfo, resultCallback)
    }

    suspend fun createResponse(
        option: GetPublicKeyCredentialOption,
        callingAppInfo: CallingAppInfo,
        resultCallback: suspend (Intent) -> Unit
    ) {
        val ids = option.getIds()
        val content = itemsRepository.getItemsDecrypted()
            .map { it.content }
            .filterIsInstance<ItemContent.Passkey>()
            .firstOrNull { ids.contains(it.credentialId?.trimBase64()) }

        if (content == null) {
            error(resultCallback)
            return
        }

        val privateKey = stringToPrivateKey(content.privateKey.clearText)
        val credentialId = content.credentialId?.decodeBase64() ?: ByteArray(0)
        val userHandle = content.userHandle?.decodeBase64() ?: ByteArray(0)

        val publicKeyRequestOptions = PublicKeyCredentialRequestOptions(option.requestJson)
        val origin = appInfoToOrigin(callingAppInfo)
        val packageName = callingAppInfo.packageName
        val clientDataHash = option.clientDataHash

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
        signature.initSign(privateKey)
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