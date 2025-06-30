/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.twofasapp.core.android.ktx.hexToColor
import com.twofasapp.core.android.ktx.toRgbHex
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.theme.RoundedShape12

@Composable
internal fun ChangeIconLabel(
    colorController: ColorPickerController,
    labelText: String?,
    labelColor: String?,
    onTextChange: (String) -> Unit = {},
    onColorChange: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var colorPickerInitialized = false

    OptionHeader(text = "Text (1 or 2 characters)")

    TextField(
        value = labelText.orEmpty(),
        onValueChange = {
            if (it.length <= 2) {
                onTextChange(it)
            }
        },
        placeholderText = "1 or 2 characters",
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Characters),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    )

    OptionHeader(text = "Background Color")

    HsvColorPicker(
        controller = colorController,
        initialColor = labelColor?.hexToColor() ?: MdtTheme.color.surfaceContainer,
        onColorChanged = { colorEnvelope ->
            if (colorPickerInitialized) {
                focusManager.clearFocus()
                onColorChange(colorEnvelope.color.toRgbHex())
            }

            colorPickerInitialized = true
        },
        modifier = Modifier
            .size(180.dp)
            .padding(vertical = 16.dp),
    )

    BrightnessSlider(
        initialColor = labelColor?.hexToColor() ?: MdtTheme.color.surfaceContainer,
        controller = colorController,
        borderColor = MdtTheme.color.surface,
        wheelRadius = 10.dp,
        modifier = Modifier
            .clip(RoundedShape12)
            .fillMaxWidth(0.7f)
            .background(labelColor?.hexToColor() ?: MdtTheme.color.surfaceContainer)
            .height(30.dp),
    )

    Spacer(modifier = Modifier.height(16.dp))

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        predefinedPalette.forEach { color ->
            item {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { colorController.selectByColor(color = color.hexToColor(), true) }
                        .border(1.dp, MdtTheme.color.outline, CircleShape)
                        .padding(4.dp)
                        .background(color.hexToColor(), CircleShape),
                )
            }
        }
    }
}

private val predefinedPalette = listOf(
    "#ED1C24",
    "#FF7A00",
    "#FFBA0A",
    "#03BF38",
    "#2FCFBC",
    "#7F9CFF",
    "#5E5CE6",
    "#ca49de",
    "#8C49DE",
    "#bd8857",
)

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        ChangeIconLabel(
            colorController = rememberColorPickerController(),
            labelText = "XY",
            labelColor = null,
        )
    }
}