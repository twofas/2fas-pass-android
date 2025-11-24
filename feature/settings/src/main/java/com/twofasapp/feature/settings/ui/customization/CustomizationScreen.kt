/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.customization

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionHeaderContentPaddingFirst
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.dialog.InputDialog
import com.twofasapp.core.design.foundation.dialog.InputValidation
import com.twofasapp.core.design.foundation.dialog.ListRadioDialog
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.settings.domain.ItemClickAction
import com.twofasapp.feature.settings.R
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CustomizationScreen(
    viewModel: CustomizationViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onThemeChange = { viewModel.updateTheme(it) },
        onDynamicColorsChange = { viewModel.updateDynamicColors(it) },
        onItemClickActionChange = { viewModel.updateItemClickAction(it) },
        onDeviceNameChange = { viewModel.updateDeviceName(it) },
        onRestoreDefaultDeviceName = { viewModel.restoreDefaultDeviceName() },
        onManageTagsClick = { deeplinks.openScreen(Screen.ManageTags) },
    )
}

@Composable
private fun Content(
    uiState: CustomizationUiState,
    onThemeChange: (SelectedTheme) -> Unit = {},
    onDynamicColorsChange: (Boolean) -> Unit = {},
    onItemClickActionChange: (ItemClickAction) -> Unit = {},
    onDeviceNameChange: (String) -> Unit = {},
    onRestoreDefaultDeviceName: () -> Unit = {},
    onManageTagsClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    var showItemClickActionDialog by remember { mutableStateOf(false) }
    var showDeviceNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsEntryCustomization) },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionHeader(
                text = strings.settingsEntryTheme,
                contentPadding = OptionHeaderContentPaddingFirst,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedShape16)
                    .background(MdtTheme.color.surfaceContainerLow)
                    .padding(vertical = 24.dp),
            ) {
                SelectedTheme.entries.forEach { theme ->
                    ThemeOption(
                        modifier = Modifier
                            .weight(1f),
                        theme = theme,
                        selected = uiState.selectedTheme == theme,
                        onClick = { onThemeChange(theme) },
                    )
                }
            }

            OptionSwitch(
                checked = uiState.dynamicColors,
                title = strings.settingsEntryDynamicColors,
                subtitle = strings.settingsEntryDynamicColorsDesc,
                icon = MdtIcons.Palette,
                onToggle = onDynamicColorsChange,
            )

            OptionHeader(
                text = "Tags",
            )

            OptionEntry(
                title = strings.settingsEntryManageTags,
                subtitle = strings.settingsEntryManageTagsDescription,
                icon = MdtIcons.Tag,
                onClick = onManageTagsClick,
            )

            OptionHeader(
                text = strings.settingsEntryConvenience,
            )

            OptionEntry(
                title = strings.settingsEntryLoginClickAction,
                subtitle = uiState.itemClickAction.asString(),
                icon = MdtIcons.Touch,
                onClick = { showItemClickActionDialog = true },
            )

            OptionEntry(
                title = strings.settingsEntryDeviceNickname,
                subtitle = uiState.deviceName,
                icon = MdtIcons.Smartphone,
                onClick = { showDeviceNameDialog = true },
            )
        }
    }

    if (showItemClickActionDialog) {
        ListRadioDialog(
            title = MdtLocale.strings.settingsEntryLoginClickAction,
            body = MdtLocale.strings.settingsEntryLoginClickActionDesc,
            icon = MdtIcons.Touch,
            onDismissRequest = { showItemClickActionDialog = false },
            options = ItemClickAction.entries.map { it.asString() },
            selectedIndex = ItemClickAction.entries.indexOf(uiState.itemClickAction),
            onOptionSelected = { index, _ -> onItemClickActionChange(ItemClickAction.entries[index]) },
        )
    }

    if (showDeviceNameDialog) {
        InputDialog(
            onDismissRequest = { showDeviceNameDialog = false },
            title = MdtLocale.strings.settingsEntryDeviceNickname,
            body = MdtLocale.strings.settingsEntryDeviceNicknameDesc,
            icon = MdtIcons.Smartphone,
            neutral = "Use Default",
            onPositive = onDeviceNameChange,
            onNeutral = onRestoreDefaultDeviceName,
            prefill = uiState.deviceName,
            label = "Name",
            validate = { text ->
                if (text.isBlank()) {
                    InputValidation.Invalid("Name can not be empty")
                } else if (text.length > 128) {
                    InputValidation.Invalid("Max length is 128 characters")
                } else {
                    InputValidation.Valid
                }
            },
        )
    }
}

@Composable
private fun ThemeOption(
    modifier: Modifier = Modifier,
    theme: SelectedTheme,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = when (theme) {
                SelectedTheme.Auto -> painterResource(R.drawable.img_theme_auto)
                SelectedTheme.Light -> painterResource(R.drawable.img_theme_light)
                SelectedTheme.Dark -> painterResource(R.drawable.img_theme_dark)
            },
            contentDescription = null,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, if (selected) MdtTheme.color.primary else MdtTheme.color.transparent, RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .padding(4.dp),
        )

        Space(8.dp)

        Text(
            text = theme.name,
            style = MdtTheme.typo.titleMedium,
        )
    }
}

@Composable
private fun ItemClickAction.asString(): String {
    return when (this) {
        ItemClickAction.View -> MdtLocale.strings.homeItemView
        ItemClickAction.Edit -> MdtLocale.strings.homeItemEdit
        ItemClickAction.Copy -> MdtLocale.strings.commonCopy
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = CustomizationUiState(),
        )
    }
}