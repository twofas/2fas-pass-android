package com.twofasapp.feature.credentialprovider.handler

import java.security.PrivateKey

object KeySingleton {
    var key: PrivateKey? = null
    var userHandle: ByteArray? = null
    var credentialId: ByteArray? = null
}