package com.twofasapp.feature.credentialprovider.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.twofasapp.feature.credentialprovider.handler.PassKeyCreateHandler
import com.twofasapp.feature.credentialprovider.handler.PassKeyGetHandler
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PassCredentialProviderActivity : AppCompatActivity() {

    private val passKeyCreateHandler by inject<PassKeyCreateHandler>()
    private val passKeyGetHandler by inject<PassKeyGetHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            passKeyCreateHandler.handle(intent) { createResultIntent ->
                if (createResultIntent == null) {
                    passKeyGetHandler.handle(intent) { getResultIntent ->
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

}