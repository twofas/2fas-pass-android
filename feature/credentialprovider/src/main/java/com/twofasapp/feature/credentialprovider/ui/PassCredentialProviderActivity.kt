package com.twofasapp.feature.credentialprovider.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.twofasapp.feature.credentialprovider.handler.PassKeyCreateHandler
import org.koin.android.ext.android.inject

class PassCredentialProviderActivity : AppCompatActivity() {

    private val passKeyHandler by inject<PassKeyCreateHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passKeyHandler.handle(intent, this) { resultIntent ->
            resultIntent?.let {
                setResult(RESULT_OK, it)
            }
            finish()
        }
    }

}