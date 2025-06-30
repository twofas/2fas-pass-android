/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.password

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.common.domain.PasswordGenerator
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.RoundedShape12
import kotlin.math.roundToInt

@Composable
fun PasswordGeneratorForm(
    modifier: Modifier = Modifier,
    settings: PasswordGeneratorSettings,
    onPasswordChange: (String, PasswordGeneratorSettings) -> Unit = { _, _ -> },
) {
    val context = LocalContext.currentActivity
    var sliderPosition by remember { mutableFloatStateOf(settings.length.toFloat()) }
    var characters by remember { mutableIntStateOf(settings.length) }
    var password by remember { mutableStateOf("") }
    var generate by remember { mutableIntStateOf(0) }
    var requireDigits by remember { mutableStateOf(settings.requireDigits) }
    var requireUppercase by remember { mutableStateOf(settings.requireUppercase) }
    var requireSpecial by remember { mutableStateOf(settings.requireSpecial) }

    LaunchedEffect(characters, generate) {
        with(
            PasswordGeneratorSettings(
                length = characters,
                requireDigits = requireDigits,
                requireUppercase = requireUppercase,
                requireSpecial = requireSpecial,
            ),
        ) {
            password = PasswordGenerator.generatePassword(this)
            onPasswordChange(password, this)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .background(MdtTheme.color.surfaceContainer, RoundedShape12)
                .padding(horizontal = 16.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = passwordColorized(password),
                style = MdtTheme.typo.bodyLarge.copy(fontSize = 18.sp),
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                text = "Copy",
                modifier = Modifier.weight(1f),
                leadingIcon = MdtIcons.Copy,
                height = 36.dp,
                onClick = { context.copyToClipboard(text = password, label = "Generated Password", isSensitive = true) },
            )
            Button(
                text = "Generate",
                modifier = Modifier.weight(1f),
                leadingIcon = MdtIcons.Refresh,
                height = 36.dp,
                onClick = { generate += 1 },
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text(
                    text = "Characters",
                    style = MdtTheme.typo.medium.base,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = characters.toString(),
                    style = MdtTheme.typo.regular.base,
                    color = MdtTheme.color.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }

            Slider(
                modifier = Modifier.weight(1f),
                value = sliderPosition,
                onValueChange = { value ->
                    with(value.roundToInt()) {
                        sliderPosition = this.toFloat()
                        characters = this
                    }
                },
                valueRange = 6f..64f,
                steps = 64 - 6 - 1,
                colors = SliderDefaults.colors(
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
            )
        }

        OptionSwitch(
            title = "Digits (0-9)",
            checked = requireDigits,
            onToggle = {
                requireDigits = it
                generate += 1
            },
            contentPadding = PaddingValues(horizontal = 16.dp),
        )

        OptionSwitch(
            title = "Uppercase characters",
            checked = requireUppercase,
            onToggle = {
                requireUppercase = it
                generate += 1
            },
            contentPadding = PaddingValues(horizontal = 16.dp),
        )

        OptionSwitch(
            title = "Special characters",
            checked = requireSpecial,
            onToggle = {
                requireSpecial = it
                generate += 1
            },
            contentPadding = PaddingValues(horizontal = 16.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        PasswordGeneratorForm(
            modifier = Modifier
                .fillMaxWidth(),
            settings = PasswordGeneratorSettings(),
        )
    }
}