/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.ui.save

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.getSafelyParcelable
import com.twofasapp.core.android.ktx.toastShort
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.autofill.service.builders.IntentBuilders.EXTRA_SAVE_LOGIN_DATA
import com.twofasapp.feature.autofill.service.domain.SaveLoginData
import com.twofasapp.feature.itemform.ItemForm
import com.twofasapp.feature.itemform.ItemFormListener
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AutofillSaveLoginScreen(
    viewModel: AutofillSaveLoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.currentActivity
    val strings = MdtLocale.strings
    val saveLoginData = activity.intent.extras.getSafelyParcelable<SaveLoginData>(EXTRA_SAVE_LOGIN_DATA)

    if (saveLoginData == null) {
        activity.finishAffinity()
        return
    }

    LaunchedEffect(Unit) {
        viewModel.initLogin(saveLoginData)
    }

    uiState.initialItem?.let {
        Content(
            uiState = uiState,
            onItemUpdated = viewModel::updateItem,
            onIsValidUpdated = viewModel::updateIsValid,
            onSaveClick = {
                viewModel.save(
                    onComplete = {
                        activity.finishAndRemoveTask()
                        activity.toastShort(strings.autofillSaveLoginToastSuccess)
                    },
                )
            },
        )
    }
}

@Composable
private fun Content(
    uiState: AutofillSaveLoginUiState,
    onItemUpdated: (Item) -> Unit = {},
    onIsValidUpdated: (Boolean) -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    Scaffold(
        topBar = {
            TopAppBar(
                title = strings.autofillSaveLoginTitle,
                actions = {
                    Button(
                        text = strings.commonSave,
                        style = ButtonStyle.Text,
                        onClick = onSaveClick,
                        enabled = uiState.isValid,
                    )
                },
            )
        },
    ) { padding ->
        uiState.initialItem?.let {
            ItemForm(
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
                initialItem = it,
                listener = object : ItemFormListener {
                    override fun onItemUpdated(item: Item) {
                        onItemUpdated(item)
                    }

                    override fun onIsValidUpdated(valid: Boolean) {
                        onIsValidUpdated(valid)
                    }
                },
            )
        }
    }
}