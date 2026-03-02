package com.twofasapp.feature.credentialprovider.handler

import android.annotation.SuppressLint
import androidx.credentials.webauthn.FidoPublicKeyCredential
import org.json.JSONObject

@SuppressLint("RestrictedApi")
class PassFidoPublicKeyCredential(
    private val fidoPublicKeyCredential: FidoPublicKeyCredential
) {

    fun json(): String {
        val json = fidoPublicKeyCredential.json()
        val jsonObject = JSONObject(json)
        jsonObject.put("clientExtensionResults", extensionJson())
        return jsonObject.toString()
    }

    private fun extensionJson(): JSONObject {
        val json = JSONObject()
        json.put("credProps", credPropsJson())
        return json
    }

    private fun credPropsJson(): JSONObject {
        val response = JSONObject()
        response.put("rk", true)
        return response
    }
}