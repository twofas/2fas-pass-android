/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.itemform.modals.changeicon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.common.domain.ItemUri
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemImage
import com.twofasapp.core.design.feature.items.LoginItemContentPreview
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.modal.ModalHeaderProperties
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale

internal data class ChangeIconState(
    val iconType: IconType,
    val iconUriIndex: Int?,
    val customImageUrl: String?,
    val labelText: String?,
    val labelColor: String?,
    val defaultLabelText: String,
    val uris: List<ItemUri>,
)

@Composable
internal fun ChangeIconModal(
    onDismissRequest: () -> Unit,
    state: ChangeIconState,
    onIconTypeChange: (IconType) -> Unit = {},
    onIconUriIndexChange: (Int?) -> Unit = {},
    onLabelTextChange: (String?) -> Unit = {},
    onLabelColorChange: (String?) -> Unit = {},
    onImageUrlChange: (String?) -> Unit = {},
) {
    val strings = MdtLocale.strings
    Modal(
        onDismissRequest = onDismissRequest,
        dismissOnSwipe = false,
        headerProperties = ModalHeaderProperties(showCloseButton = true, showDragHandle = false),
        headerText = strings.customizeIcon,
    ) { dismissAction ->
        Content(
            state = state,
            onSave = { iconType, iconUriIndex, labelText, labelColor, imageUrl ->
                dismissAction {
                    if (iconType != state.iconType) {
                        onIconTypeChange(iconType)
                    }

                    if (iconUriIndex != state.iconUriIndex) {
                        onIconUriIndexChange(iconUriIndex)
                    }

                    if (labelText.orEmpty() != state.labelText.orEmpty()) {
                        onLabelTextChange(labelText)
                    }

                    if (labelColor.orEmpty() != state.labelColor.orEmpty()) {
                        onLabelColorChange(labelColor)
                    }

                    if (imageUrl.orEmpty() != state.customImageUrl.orEmpty()) {
                        onImageUrlChange(imageUrl)
                    }
                }
            },
        )
    }
}

@Composable
private fun Content(
    state: ChangeIconState,
    onSave: (IconType, Int?, String?, String?, String?) -> Unit = { _, _, _, _, _ -> },
) {
    val colorController = rememberColorPickerController()
    val focusManager = LocalFocusManager.current
    val strings = MdtLocale.strings

    var iconType by remember { mutableStateOf(state.iconType) }

    var iconUriIndex by remember { mutableStateOf(state.iconUriIndex) }
    var labelText by remember { mutableStateOf(state.labelText ?: state.defaultLabelText) }
    var labelColor by remember { mutableStateOf(state.labelColor) }
    var imageUrl by remember { mutableStateOf(state.customImageUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp, bottom = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ItemImage(
                iconType = iconType,
                iconUrl = iconUriIndex?.let { state.uris.getOrNull(it)?.iconUrl },
                labelText = labelText,
                labelColor = labelColor,
                customImageUrl = imageUrl,
                size = 50.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .padding(horizontal = 18.dp)
                    .clip(CircleShape)
                    .border(1.dp, MdtTheme.color.outline, CircleShape),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SegmentedButton(
                    text = strings.customizeIconIcon,
                    checked = iconType == IconType.Icon,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        iconType = IconType.Icon
                        focusManager.clearFocus()
                    },
                )

                VerticalDivider(color = MdtTheme.color.outline)

                SegmentedButton(
                    text = strings.customizeIconLabelKey,
                    checked = iconType == IconType.Label,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        iconType = IconType.Label
                        focusManager.clearFocus()
                    },
                )

                VerticalDivider(color = MdtTheme.color.outline)

                SegmentedButton(
                    text = strings.changeIconSegmentImageUrl,
                    checked = iconType == IconType.CustomImageUrl,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        iconType = IconType.CustomImageUrl
                        focusManager.clearFocus()
                    },
                )
            }

            when (iconType) {
                IconType.Icon -> {
                    ChangeIconUrl(
                        uris = state.uris,
                        iconUriIndex = iconUriIndex,
                        onIndexChange = { iconUriIndex = it },
                    )
                }

                IconType.Label -> {
                    ChangeIconLabel(
                        colorController = colorController,
                        labelText = labelText,
                        labelColor = labelColor,
                        onTextChange = { labelText = it },
                        onColorChange = { labelColor = it },
                    )
                }

                IconType.CustomImageUrl -> {
                    ChangeIconCustomImageUrl(
                        imageUrl = imageUrl,
                        onUrlChange = { imageUrl = it },
                    )
                }
            }
        }

        Button(
            text = strings.commonSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding)
                .padding(top = 8.dp),
            enabled = when (iconType) {
                IconType.Icon -> iconUriIndex != null
                IconType.Label -> true
                IconType.CustomImageUrl -> imageUrl.isNullOrBlank().not()
            },
            onClick = {
                onSave(iconType, iconUriIndex, labelText, labelColor, imageUrl)
            },
        )
    }
}

@Composable
private fun SegmentedButton(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (checked) MdtTheme.color.secondaryContainer else MdtTheme.color.surface)
            .clickable { onClick() },
    ) {
        TextIcon(
            text = text,
            textAlign = TextAlign.Center,
            style = MdtTheme.typo.medium.sm,
            color = if (checked) MdtTheme.color.onSecondaryContainer else MdtTheme.color.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),

        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        val loginItemContent = LoginItemContentPreview

        Content(
            state = ChangeIconState(
                iconType = loginItemContent.iconType,
                iconUriIndex = loginItemContent.iconUriIndex,
                customImageUrl = loginItemContent.customImageUrl,
                labelText = loginItemContent.labelText,
                labelColor = loginItemContent.labelColor,
                defaultLabelText = loginItemContent.defaultLabelText,
                uris = loginItemContent.uris,
            ),
        )
    }
}