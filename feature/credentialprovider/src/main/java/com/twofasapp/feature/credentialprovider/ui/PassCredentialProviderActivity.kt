package com.twofasapp.feature.credentialprovider.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.twofasapp.feature.credentialprovider.handler.PassKeyCreateHandler
import com.twofasapp.feature.credentialprovider.handler.PassKeyGetHandler
import org.koin.android.ext.android.inject

class PassCredentialProviderActivity : AppCompatActivity() {

    private val passKeyCreateHandler by inject<PassKeyCreateHandler>()
    private val passKeyGetHandler by inject<PassKeyGetHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passKeyCreateHandler.handle(intent, this) { createResultIntent ->
            if (createResultIntent == null) {
                passKeyGetHandler.handle(intent, this) { getResultIntent ->
                    getResultIntent?.let {
                        setResult(RESULT_OK, getResultIntent)
                    }
                }
            } else {
                setResult(RESULT_OK, createResultIntent)
            }
            finish()
        }
    }

}