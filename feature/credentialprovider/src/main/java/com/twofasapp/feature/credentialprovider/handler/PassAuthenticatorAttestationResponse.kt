package com.twofasapp.feature.credentialprovider.handler

import android.annotation.SuppressLint
import androidx.credentials.webauthn.AuthenticatorAttestationResponse
import androidx.credentials.webauthn.AuthenticatorResponse
import androidx.credentials.webauthn.PublicKeyCredentialCreationOptions
import com.twofasapp.core.common.ktx.encodeBase64UrlSafeNoPadding
import org.json.JSONObject
import java.security.MessageDigest

@SuppressLint("RestrictedApi")
class PassAuthenticatorAttestationResponse(
    private val response: AuthenticatorAttestationResponse,
    private val requestOptions: PublicKeyCredentialCreationOptions,
    private val credentialId: ByteArray,
    private val credentialPublicKey: ByteArray,
    private val up: Boolean,
    private val uv: Boolean,
    private val be: Boolean,
    private val bs: Boolean,
    private val spki: ByteArray,
) : AuthenticatorResponse by response {

    override fun json(): JSONObject {
        val json = response.json()
        json.addParsedAttestationObjectFieldsToJSON()
        return json
    }

    private fun JSONObject.addParsedAttestationObjectFieldsToJSON() {
        // https://www.w3.org/TR/webauthn-2/#sctn-generating-an-attestation-object
        put(
            "authenticatorData",
            authData().encodeBase64UrlSafeNoPadding(),
        )
        put("publicKeyAlgorithm", getPublicKeyAlgorithm())
        put("publicKey", spki.encodeBase64UrlSafeNoPadding())
    }

    private fun authData(): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        val rpHash = md.digest(requestOptions.rp.id.toByteArray())
        var flags = 0
        if (up) {
            flags = flags or 0x01
        }
        if (uv) {
            flags = flags or 0x04
        }
        if (be) {
            flags = flags or 0x08
        }
        if (bs) {
            flags = flags or 0x10
        }
        flags = flags or 0x40

        val aaguid = ByteArray(16) { 0 }
        val credIdLen = byteArrayOf((credentialId.size shr 8).toByte(), credentialId.size.toByte())

        val ret =
            rpHash +
                    byteArrayOf(flags.toByte()) +
                    byteArrayOf(0, 0, 0, 0) +
                    aaguid +
                    credIdLen +
                    credentialId +
                    credentialPublicKey

        return ret
    }

    private fun getPublicKeyAlgorithm(): Long {
        // Learn more here : https://www.iana.org/assignments/cose/cose.xhtml#algorithms
        return -7
    }
}