package com.twofasapp.feature.credentialprovider.handler

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Base64
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.credentials.webauthn.AuthenticatorAttestationResponse
import androidx.credentials.webauthn.Cbor
import androidx.credentials.webauthn.FidoPublicKeyCredential
import androidx.credentials.webauthn.PublicKeyCredentialCreationOptions
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.ItemContentType
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.data.main.ItemsRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

@SuppressLint("RestrictedApi")
class PassKeyCreateHandler(
    private val itemsRepository: ItemsRepository,
    private val itemEncryptionMapper: ItemEncryptionMapper,
    private val vaultCryptoScope: VaultCryptoScope,
    private val vaultsRepository: VaultsRepository
) {

    suspend fun handle(
        intent: Intent,
        resultCallback: suspend (Intent?) -> Unit
    ) {
        val request =
            PendingIntentHandler.retrieveProviderCreateCredentialRequest(intent)
        val callingRequest = request?.callingRequest

        when {
            request == null -> resultCallback(null)

            callingRequest?.type == PublicKeyCredential.TYPE_PUBLIC_KEY_CREDENTIAL && callingRequest is CreatePublicKeyCredentialRequest ->
                handle(
                    callingRequest,
                    request.callingAppInfo,
                    resultCallback
                )

            else -> error(resultCallback)
        }
    }

    private suspend fun error(resultCallback: suspend (Intent) -> Unit) {
        resultCallback(
            Intent().apply {
                PendingIntentHandler.setCreateCredentialException(
                    this,
                    CreateCredentialUnknownException(),
                )
            }
        )
    }

    private suspend fun handle(
        request: CreatePublicKeyCredentialRequest,
        callingAppInfo: CallingAppInfo,
        resultCallback: suspend (Intent) -> Unit
    ) {
        createResponse(request, callingAppInfo, resultCallback)
    }

    suspend fun createResponse(
        request: CreatePublicKeyCredentialRequest,
        callingAppInfo: CallingAppInfo,
        resultCallback: suspend (Intent) -> Unit
    ) {
        val keyPair = generateKeyPair()
        val coseKey = publicKeyToCose(keyPair.public as ECPublicKey)

        val requestOptions = PublicKeyCredentialCreationOptions(request.requestJson)
        val credentialPublicKey = Cbor().encode(coseKey)
        val origin = appInfoToOrigin(callingAppInfo)
        val up = true
        val uv = true
        val be = true
        val bs = true
        val packageName = callingAppInfo.packageName
        val clientDataHash = request.clientDataHash
        val spki = coseKeyToSPKI(coseKey)
        val credentialId = ByteArray(32)
        SecureRandom().nextBytes(credentialId)

        val item = Item.create(
            vaultId = vaultsRepository.getVault().id,
            contentType = ItemContentType.Passkey,
            content = ItemContent.Passkey.Empty.copy(
                name =  requestOptions.user.displayName,
                privateKey = SecretField.ClearText(privateKeyToString(keyPair.private)),
                userHandle = requestOptions.user.id.encodeBase64(),
                credentialId = credentialId.encodeBase64(),
                rpId = requestOptions.rp.id,
                username = requestOptions.user.name
            )
        )

        val encryptedItem = itemEncryptionMapper.encryptItem(
            item = item,
            vaultCipher = vaultCryptoScope.getVaultCipher(vaultsRepository.getVault().id),
        )

        itemsRepository.saveItem(encryptedItem)

        val response = PassAuthenticatorAttestationResponse(
            response = AuthenticatorAttestationResponse(
                requestOptions = requestOptions,
                credentialId = credentialId,
                credentialPublicKey = credentialPublicKey,
                origin = origin,
                up = up,
                uv = uv,
                be = be,
                bs = bs,
                packageName = packageName,
                clientDataHash = clientDataHash,
            ),
            requestOptions = requestOptions,
            credentialId = credentialId,
            credentialPublicKey = credentialPublicKey,
            up = up,
            uv = uv,
            be = be,
            bs = bs,
            spki = spki
        )

        val credential = PassFidoPublicKeyCredential(
            FidoPublicKeyCredential(
                rawId = credentialId,
                response = response,
                authenticatorAttachment = "platform",
            )
        )

        resultCallback(
            Intent().apply {
                val createPublicKeyCredResponse =
                    CreatePublicKeyCredentialResponse(credential.json())
                PendingIntentHandler.setCreateCredentialResponse(
                    this,
                    createPublicKeyCredResponse
                )
            }
        )
    }

    private fun generateKeyPair(): KeyPair {
        val spec = ECGenParameterSpec("secp256r1")
        val keyPairGen = KeyPairGenerator.getInstance("EC")
        keyPairGen.initialize(spec)
        return keyPairGen.genKeyPair()
    }

    private fun publicKeyToCose(key: ECPublicKey): MutableMap<Int, Any> {
        val x = bigIntToFixedArray(key.w.affineX)
        val y = bigIntToFixedArray(key.w.affineY)
        val coseKey = mutableMapOf<Int, Any>()
        coseKey[1] = 2 // EC Key type
        coseKey[3] = -7 // ES256
        coseKey[-1] = 1 // P-256 Curve
        coseKey[-2] = x // x
        coseKey[-3] = y // y
        return coseKey
    }

    private fun bigIntToFixedArray(n: BigInteger): ByteArray {
        assert(n.signum() >= 0)

        val bytes = n.toByteArray()
        // `toByteArray` will left-pad with a leading zero if the
        // most-significant bit of the first byte would otherwise be one.
        var offset = 0
        if (bytes[0] == 0x00.toByte()) {
            offset++
        }
        val bytesLen = bytes.size - offset
        assert(bytesLen <= 32)

        val output = ByteArray(32)
        System.arraycopy(bytes, offset, output, 32 - bytesLen, bytesLen)
        return output
    }

    private fun coseKeyToSPKI(coseKey: MutableMap<Int, Any>): ByteArray {
        val spkiPrefix: ByteArray = Base64.decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE", 0)
        val x = coseKey[-2] as ByteArray
        val y = coseKey[-3] as ByteArray
        return spkiPrefix + x + y
    }
}