package com.twofasapp.feature.itemform.forms.wifi

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.WifiSecurityType
import com.twofasapp.core.common.domain.clearTextOrNull
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.ItemImage
import com.twofasapp.core.design.feature.items.WifiItemContentPreview
import com.twofasapp.core.design.feature.items.WifiItemPreview
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionEntryPaddingHorizontal
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.feature.wifi.formatName
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.lazy.listItem
import com.twofasapp.core.design.foundation.menu.DropdownPicker
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.textfield.SecretField
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.TextField
import com.twofasapp.core.design.foundation.textfield.passwordColors
import com.twofasapp.core.design.foundation.topbar.TopAppBarTitle
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape24
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.itemform.ItemFormListener
import com.twofasapp.feature.itemform.ItemFormProperties
import com.twofasapp.feature.itemform.ItemFormUiState
import com.twofasapp.feature.itemform.forms.common.FormListItem
import com.twofasapp.feature.itemform.forms.common.ItemContentFormContainer
import com.twofasapp.feature.itemform.forms.common.noteItem
import com.twofasapp.feature.itemform.forms.common.securityTypePickerItem
import com.twofasapp.feature.itemform.forms.common.tagsPickerItem
import com.twofasapp.feature.itemform.forms.common.timestampInfoItem
import com.twofasapp.feature.permissions.RequestPermission
import com.twofasapp.feature.qrscan.QrScan
import kotlinx.coroutines.android.awaitFrame
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun WifiForm(
    modifier: Modifier = Modifier,
    viewModel: WifiFormViewModel = koinViewModel(),
    initialItem: Item,
    containerColor: Color = MdtTheme.color.background,
    properties: ItemFormProperties,
    listener: ItemFormListener,
) {
    val passwordVisibilityState by viewModel.passwordVisibilityState.collectAsStateWithLifecycle()
    val wifiSecurityTypeDropdownVisibilityState by viewModel.wifiSecurityTypeDropdownVisibilityState.collectAsStateWithLifecycle()
    var askForCameraPermission by remember { mutableStateOf(false) }
    var showQrModal by remember { mutableStateOf(false) }

    if (showQrModal) {
        QrScannerModal(
            onDismiss = { showQrModal = false },
            onScanned = {
                val valid = viewModel.onQrCodeScanned(it)
                if (valid) {
                    showQrModal = false
                }
            },
        )
    }

    if (askForCameraPermission) {
        RequestPermission(
            permission = Manifest.permission.CAMERA,
            rationaleEnabled = true,
            rationaleTitle = MdtLocale.strings.permissionCameraTitle,
            rationaleText = MdtLocale.strings.permissionCameraMsg,
            onGranted = {
                showQrModal = true
                askForCameraPermission = false
            },
            onDismissRequest = { askForCameraPermission = false },
        )
    }

    ItemContentFormContainer(
        viewModel = viewModel,
        initialItem = initialItem,
        properties = properties,
        listener = listener,
    ) { uiState ->
        WifiFormContent(
            modifier = modifier,
            passwordVisible = passwordVisibilityState,
            wifiSecurityTypeDropdownVisible = wifiSecurityTypeDropdownVisibilityState,
            uiState = uiState,
            containerColor = containerColor,
            onNameChange = viewModel::onNameChanged,
            onSsidChange = viewModel::onSsidChanged,
            onSecurityTypeChange = viewModel::updateSecurityType,
            onTagsChange = viewModel::updateTags,
            onNotesChange = viewModel::onNotesChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onPasswordToggleClick = viewModel::onPasswordToggleClicked,
            onHiddenChange = viewModel::onHiddenChanged,
            onWifiSecurityTypeChange = viewModel::onWifiSecurityTypeChanged,
            onWifiSecurityTypeClick = viewModel::onWifiSecurityTypeClicked,
            onWifiSecurityTypeDropdownDismiss = viewModel::onWifiSecurityTypeDropdownDismissed,
            onScanQrCodeClick = {
                askForCameraPermission = true
            },
        )
    }
}

@Composable
private fun WifiFormContent(
    passwordVisible: Boolean,
    wifiSecurityTypeDropdownVisible: Boolean,
    uiState: ItemFormUiState<ItemContent.Wifi>,
    modifier: Modifier = Modifier,
    containerColor: Color = MdtTheme.color.background,
    noteLabel: String = MdtLocale.strings.wifiNotesLabel,
    onNameChange: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordToggleClick: () -> Unit,
    onSecurityTypeChange: (SecurityType) -> Unit,
    onTagsChange: (List<String>) -> Unit,
    onNotesChange: (String) -> Unit,
    onHiddenChange: (Boolean) -> Unit,
    onWifiSecurityTypeChange: (WifiSecurityType) -> Unit,
    onWifiSecurityTypeClick: () -> Unit,
    onWifiSecurityTypeDropdownDismiss: () -> Unit,
    onScanQrCodeClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(containerColor),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = ScreenPadding,
            end = ScreenPadding,
            bottom = ScreenPadding,
            top = 8.dp,
        ),
    ) {
        nameTextField(uiState = uiState, onValueChange = onNameChange)
        scanQrCode(onClick = onScanQrCodeClick)
        ssidTextField(uiState = uiState, onValueChange = onSsidChange)
        passwordTextField(
            passwordVisible = passwordVisible,
            uiState = uiState,
            onValueChange = onPasswordChange,
            onToggleClick = onPasswordToggleClick,
        )
        wifiSecurityTypePicker(
            uiState = uiState,
            onValueChange = onWifiSecurityTypeChange,
            dropdownVisible = wifiSecurityTypeDropdownVisible,
            onDropdownDismiss = onWifiSecurityTypeDropdownDismiss,
            onClick = onWifiSecurityTypeClick,
        )
        hiddenSwitch(uiState = uiState, onValueChange = onHiddenChange)
        securityTypePickerItem(
            item = uiState.item,
            onSecurityTypeChange = onSecurityTypeChange,
        )
        tagsPickerItem(
            item = uiState.item,
            tags = uiState.tags,
            onTagsChange = onTagsChange,
        )
        noteItem(
            label = noteLabel,
            notes = uiState.itemContent?.notes,
            onNotesChange = onNotesChange,
        )
        timestampInfoItem(item = uiState.item)
    }
}

private fun LazyListScope.nameTextField(
    uiState: ItemFormUiState<ItemContent.Wifi>,
    onValueChange: (String) -> Unit,
) {
    listItem(FormListItem.Field("Name")) {
        TextField(
            value = uiState.itemContent?.name ?: "",
            onValueChange = onValueChange,
            labelText = MdtLocale.strings.wifiNameLabel,
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            trailingIcon = {
                ItemImage(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedShape12),
                    item = uiState.item,
                    size = 42.dp,
                )
            },
        )
    }
}

private fun LazyListScope.ssidTextField(
    uiState: ItemFormUiState<ItemContent.Wifi>,
    onValueChange: (String) -> Unit,
) {
    listItem(FormListItem.Field("Ssid")) {
        TextField(
            value = uiState.itemContent?.ssid ?: "",
            onValueChange = onValueChange,
            labelText = MdtLocale.strings.wifiSsidLabel,
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )
    }
}

private fun LazyListScope.passwordTextField(
    passwordVisible: Boolean,
    uiState: ItemFormUiState<ItemContent.Wifi>,
    onValueChange: (String) -> Unit,
    onToggleClick: () -> Unit,
) {
    listItem(FormListItem.Field("Password")) {
        TextField(
            value = uiState.itemContent?.password?.clearTextOrNull ?: "",
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
            labelText = MdtLocale.strings.wifiPasswordLabel,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            visualTransformation = VisualTransformation.SecretField(
                passwordVisible,
                passwordColors,
            ),
            trailingIcon = {
                ActionsRow(
                    useHorizontalPadding = true,
                ) {
                    SecretFieldTrailingIcon(
                        visible = passwordVisible,
                        onToggle = onToggleClick,
                    )
                }
            },
        )
    }
}

private fun LazyListScope.wifiSecurityTypePicker(
    dropdownVisible: Boolean,
    uiState: ItemFormUiState<ItemContent.Wifi>,
    onValueChange: (WifiSecurityType) -> Unit,
    onDropdownDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    listItem(FormListItem.Field("WifiSecurityType")) {
        DropdownPicker(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(),
            value = uiState.itemContent?.securityType?.formatName() ?: "",
            label = MdtLocale.strings.wifiSecurityTypeLabel,
            dropdownVisible = dropdownVisible,
            testTag = "wifiSecurityTypeDropdown",
            onShowDropdown = onClick,
            onDismissDropdown = onDropdownDismiss,
        ) {
            WifiSecurityType.values().forEach { wifiSecurityType ->
                WifiSecurityTypeMenuItem(
                    text = wifiSecurityType.formatName(),
                    checked = wifiSecurityType == (
                        uiState.itemContent?.securityType
                            ?: WifiSecurityType.None
                        ),
                    onClick = { onValueChange(wifiSecurityType) },
                )
            }
        }
    }
}

@Composable
private fun WifiSecurityTypeMenuItem(text: String, checked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                ),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            text = text,
            style = MdtTheme.typo.bodyLarge,
            color = MdtTheme.color.onSurface,
        )
        CheckIcon(
            checked = checked,
        )
    }
}

private fun LazyListScope.scanQrCode(onClick: () -> Unit) {
    listItem(FormListItem.Field("QrCode")) {
        OptionEntry(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedShape12)
                .background(MdtTheme.color.surfaceContainer)
                .clickable(onClick = onClick)
                .padding(horizontal = OptionEntryPaddingHorizontal),
            external = true,
            externalIcon = MdtIcons.QrScanner,
            title = MdtLocale.strings.wifiQrScanTitle,
            contentPadding = ZeroPadding,
        )
    }
}

private fun LazyListScope.hiddenSwitch(
    uiState: ItemFormUiState<ItemContent.Wifi>,
    onValueChange: (Boolean) -> Unit,
) {
    listItem(FormListItem.Field("Hidden")) {
        OptionSwitch(
            title = MdtLocale.strings.wifiHiddenNetworkLabel,
            checked = uiState.itemContent?.hidden ?: false,
            testTag = "wifiHiddenNetworkSwitch",
            onToggle = onValueChange,
        )
    }
}

@Composable
private fun QrScannerModal(onDismiss: () -> Unit, onScanned: (String) -> Unit) {
    Modal(
        onDismissRequest = onDismiss,
    ) {
        LaunchedEffect(Unit) {
            awaitFrame()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(ScreenPadding),
        ) {
            TopAppBarTitle(text = MdtLocale.strings.wifiQrScanAction)
            QrScan(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedShape24),
                requireAuth = false,
                onScanned = onScanned,
            )
            Text(
                text = MdtLocale.strings.wifiQrScanTitle,
                color = MdtTheme.color.tertiary,
                style = MdtTheme.typo.regular.sm,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun WifiFormContentPreview() {
    PreviewTheme {
        WifiFormContent(
            passwordVisible = false,
            wifiSecurityTypeDropdownVisible = false,
            uiState = ItemFormUiState(
                item = WifiItemPreview,
                itemContent = WifiItemContentPreview,
            ),
            onNameChange = {},
            onSsidChange = {},
            onSecurityTypeChange = {},
            onTagsChange = {},
            onNotesChange = {},
            onPasswordChange = {},
            onPasswordToggleClick = {},
            onHiddenChange = {},
            onWifiSecurityTypeChange = {},
            onWifiSecurityTypeClick = {},
            onWifiSecurityTypeDropdownDismiss = {},
            onScanQrCodeClick = {},
        )
    }
}

@Preview
@Composable
private fun WifiFormContentTypeDropdownPreview() {
    PreviewTheme {
        WifiFormContent(
            passwordVisible = false,
            wifiSecurityTypeDropdownVisible = true,
            uiState = ItemFormUiState(
                item = WifiItemPreview,
                itemContent = WifiItemContentPreview,
            ),
            onNameChange = {},
            onSsidChange = {},
            onSecurityTypeChange = {},
            onTagsChange = {},
            onNotesChange = {},
            onPasswordChange = {},
            onPasswordToggleClick = {},
            onHiddenChange = {},
            onWifiSecurityTypeChange = {},
            onWifiSecurityTypeClick = {},
            onWifiSecurityTypeDropdownDismiss = {},
            onScanQrCodeClick = {},
        )
    }
}

@Preview
@Composable
private fun WifiSecurityTypeMenuItemPreview() {
    PreviewTheme {
        Column {
            WifiSecurityTypeMenuItem(
                text = WifiSecurityType.None.value,
                checked = true,
                onClick = {},
            )
            WifiSecurityTypeMenuItem(
                text = WifiSecurityType.Wpa2.value,
                checked = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun QrScannerModalPreview() {
    PreviewTheme {
        QrScannerModal(
            onDismiss = {},
            onScanned = {},
        )
    }
}