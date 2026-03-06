package com.twofasapp.feature.credentialprovider.handler

import androidx.credentials.provider.CallingAppInfo
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeBase64
import com.twofasapp.core.common.ktx.encodeBase64UrlSafeNoPadding
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

fun appInfoToOrigin(info: CallingAppInfo): String {
    val cert = info.signingInfo.apkContentsSigners[0].toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val certHash = md.digest(cert)
    return "android:apk-key-hash:${certHash.encodeBase64UrlSafeNoPadding()}"
}

fun privateKeyToString(privateKey: PrivateKey): String {
    val encoded = privateKey.encoded
    return encoded.encodeBase64()
}

fun stringToPrivateKey(key: String): PrivateKey {
    val decoded = key.decodeBase64()
    val keySpec = PKCS8EncodedKeySpec(decoded)
    val keyFactory = KeyFactory.getInstance("EC")
    return keyFactory.generatePrivate(keySpec)
}