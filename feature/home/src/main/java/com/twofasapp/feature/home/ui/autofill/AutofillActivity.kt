/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.home.ui.autofill

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.inline.InlinePresentationSpec
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.twofasapp.core.android.ktx.applyAppTheme
import com.twofasapp.core.android.ktx.enableThemedEdgeToEdge
import com.twofasapp.core.android.ktx.makeWindowSecure
import com.twofasapp.data.settings.SettingsRepository
import com.twofasapp.feature.autofill.service.domain.AutofillLogin
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.autofill.service.parser.NodeStructure
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.security.SecureRandom

class AutofillActivity : AppCompatActivity() {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = runBlocking { settingsRepository.observeSelectedTheme().first() }

        lifecycleScope.launch {
            settingsRepository.observeSelectedTheme().collect { theme ->
                applyAppTheme(theme)
            }
        }

        enableThemedEdgeToEdge(theme = theme)

        lifecycleScope.launch {
            settingsRepository.observeScreenCaptureEnabled().collect { allow ->
                makeWindowSecure(allow = allow)
            }
        }

        super.onCreate(savedInstanceState)

        setContent {
            window.isNavigationBarContrastEnforced = false

            AutofillContainer(
                startScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.extras!!.getSerializable(EXTRA_START_SCREEN, StartScreen::class.java) ?: StartScreen.PickLogin
                } else {
                    @Suppress("DEPRECATION")
                    intent.extras!!.getSerializable(EXTRA_START_SCREEN) as StartScreen
                },
            )
        }
    }

    enum class StartScreen {
        Authenticate, PickLogin, SaveLogin
    }

    companion object {
        const val EXTRA_START_SCREEN = "startScreen"
        const val EXTRA_NODE_STRUCTURE = "nodeStructure"
        const val EXTRA_INLINE_PRESENTATION_SPEC = "inlinePresentationSpec"
        const val EXTRA_LOGIN = "login"
        const val EXTRA_SAVE_LOGIN_DATA = "saveLoginData"

        @SuppressLint("NewApi")
        fun createAuthPendingIntent(
            context: Context,
            nodeStructure: NodeStructure?,
            inlinePresentationSpec: InlinePresentationSpec?,
            login: AutofillLogin,
        ): PendingIntent {
            val intent = Intent(context, AutofillActivity::class.java).apply {
                putExtra(EXTRA_NODE_STRUCTURE, nodeStructure)
                putExtra(EXTRA_INLINE_PRESENTATION_SPEC, inlinePresentationSpec)
                putExtra(EXTRA_LOGIN, login)
                putExtra(EXTRA_START_SCREEN, StartScreen.Authenticate)
            }

            return PendingIntent.getActivity(
                context,
                login.id.hashCode(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        @SuppressLint("NewApi")
        fun createPickerPendingIntent(
            context: Context,
            nodeStructure: NodeStructure,
            inlinePresentationSpec: InlinePresentationSpec?,
        ): PendingIntent {
            val intent = Intent(context, AutofillActivity::class.java).apply {
                putExtra(EXTRA_NODE_STRUCTURE, nodeStructure)
                putExtra(EXTRA_INLINE_PRESENTATION_SPEC, inlinePresentationSpec)
                putExtra(EXTRA_START_SCREEN, StartScreen.PickLogin)
            }

            return PendingIntent.getActivity(
                context,
                SecureRandom().nextInt(),
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        @SuppressLint("NewApi")
        fun createSaveLoginIntent(
            context: Context,
            saveLoginData: SaveLoginData,
        ): Intent {
            return Intent(context, AutofillActivity::class.java).apply {
                putExtra(EXTRA_START_SCREEN, StartScreen.SaveLogin)
                putExtra(EXTRA_SAVE_LOGIN_DATA, saveLoginData)

                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
        }
    }
}