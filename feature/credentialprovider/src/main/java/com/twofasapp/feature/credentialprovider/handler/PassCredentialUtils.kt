package com.twofasapp.feature.credentialprovider.handler

import androidx.credentials.provider.CallingAppInfo
import com.twofasapp.core.common.ktx.encodeBase64UrlSafeNoPadding
import java.security.MessageDigest

fun appInfoToOrigin(info: CallingAppInfo): String {
    val cert = info.signingInfo.apkContentsSigners[0].toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val certHash = md.digest(cert)
    return "android:apk-key-hash:${certHash.encodeBase64UrlSafeNoPadding()}"
}