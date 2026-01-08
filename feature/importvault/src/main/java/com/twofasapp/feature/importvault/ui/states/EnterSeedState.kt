/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.importvault.ui.states

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.crypto.WordList
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.headers.ScreenHeader
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.locale.MdtLocale

@Composable
fun EnterSeedState(
    words: List<String>,
    seedError: String? = null,
    onWordsUpdated: (List<String>) -> Unit = {},
    onErrorDismissed: () -> Unit = {},
    onCtaClick: (List<String>) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var errors by remember { mutableStateOf(List(15) { false }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = MdtLocale.strings.restoreManualKeyInputTitle,
            description = MdtLocale.strings.restoreManualKeyInputDescription,
        )

        Space(12.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(15) { index ->
                TextField(
                    value = words[index],
                    onValueChange = { onWordsUpdated(words.toMutableList().apply { this[index] = it }) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused.not() && words[index].isNotEmpty()) {
                                errors = errors.toMutableList()
                                    .apply { this[index] = WordList.words.contains(words[index].trim().lowercase()).not() }
                            } else {
                                errors = errors.toMutableList().apply { this[index] = false }
                            }
                        },
                    labelText = MdtLocale.strings.restoreManualWord.format(index + 1),
                    isError = errors[index],
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MdtTheme.color.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MdtTheme.typo.labelMediumProminent,
                            )
                        }
                    },
                    singleLine = true,
                    keyboardActions = if (index == words.size - 1) {
                        KeyboardActions(
                            onDone = { focusManager.clearFocus() },
                        )
                    } else {
                        KeyboardActions.Default
                    },
                    keyboardOptions = if (index == words.size - 1) {
                        KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        )
                    } else {
                        KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Next,
                        )
                    },
                )
            }

            Space(12.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MdtTheme.color.background)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                text = MdtLocale.strings.commonContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = words.size == 15 && words.all { it.isNotBlank() } && errors.all { it.not() },
                onClick = { onCtaClick(words) },
            )
        }
    }

    if (seedError.isNullOrBlank().not()) {
        InfoDialog(
            title = MdtLocale.strings.commonError,
            body = seedError!!,
            onDismissRequest = onErrorDismissed,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        EnterSeedState(words = List(15) { "" })
    }
}