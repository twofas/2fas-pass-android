package com.twofasapp.feature.home.ui.itemdetails.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.common.domain.SecretField
import com.twofasapp.core.common.domain.Tag
import com.twofasapp.core.common.domain.items.Item
import com.twofasapp.core.common.domain.items.ItemContent
import com.twofasapp.core.common.domain.items.createConnectIntent
import com.twofasapp.core.common.domain.items.qrCodeContent
import com.twofasapp.core.common.domain.items.supportConnect
import com.twofasapp.core.common.domain.items.supportQrCodeContent
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.items.WifiItemContentPreview
import com.twofasapp.core.design.feature.items.WifiItemPreview
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionEntryPaddingHorizontal
import com.twofasapp.core.design.feature.wifi.formatName
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.qr.QrCode
import com.twofasapp.core.design.foundation.qr.QrCodeDefaults
import com.twofasapp.core.design.foundation.text.secretAnnotatedString
import com.twofasapp.core.design.foundation.textfield.SecretFieldTrailingIcon
import com.twofasapp.core.design.foundation.textfield.passwordColorized
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.home.ui.itemdetails.SecretFieldType
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsEntry
import com.twofasapp.feature.home.ui.itemdetails.components.ItemDetailsHeader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun ColumnScope.WifiContent(
    item: Item,
    tags: ImmutableList<Tag>,
    content: ItemContent.Wifi,
    decryptedFields: Map<SecretFieldType, String>,
    onToggleSecretField: (SecretFieldType, SecretField?) -> Unit,
    onCopySecretField: (SecretField?, (String) -> Unit) -> Unit,
    onScrollToBottom: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showWifiQrCode by remember { mutableStateOf(false) }
    var showConnectWifi by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            showWifiQrCode = false
        }
    }

    LaunchedEffect(showConnectWifi, decryptedFields[SecretFieldType.WifiConnectPassword]) {
        if (showConnectWifi.not()) {
            return@LaunchedEffect
        }

        if (content.password == null) {
            context.startActivity(content.createConnectIntent(null))
            showConnectWifi = false
        } else {
            decryptedFields[SecretFieldType.WifiConnectPassword]?.let { password ->
                context.startActivity(content.createConnectIntent(password))
                showConnectWifi = false
            }
        }
    }

    ItemDetailsHeader(
        item = item,
        tags = tags,
        subtitle = MdtLocale.strings.wifiFieldHiddenValue.takeIf { content.hidden },
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        content.ssid?.let { ssid ->
            ItemDetailsEntry(
                title = MdtLocale.strings.wifiNetworkHeader,
                subtitle = ssid,
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("wifiContentCopySsidButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(ssid) },
                    )
                },
            )
        }

        content.password?.let { password ->
            ItemDetailsEntry(
                title = MdtLocale.strings.wifiPasswordLabel,
                subtitleAnnotated = decryptedFields[SecretFieldType.WifiPassword]?.let {
                    passwordColorized(password = it)
                } ?: secretAnnotatedString(),
                actions = {
                    SecretFieldTrailingIcon(
                        visible = decryptedFields[SecretFieldType.WifiPassword] != null,
                        onToggle = { onToggleSecretField(SecretFieldType.WifiPassword, password) },
                    )

                    IconButton(
                        modifier = Modifier.testTag("wifiContentCopyPasswordButton"),
                        icon = MdtIcons.Copy,
                        onClick = {
                            onCopySecretField(password) { decrypted ->
                                context.copyToClipboard(text = decrypted, isSensitive = true)
                            }
                        },
                    )
                },
            )
        }

        ItemDetailsEntry(
            title = MdtLocale.strings.wifiSecurityTypeLabel,
            subtitle = content.securityType.formatName(),
        )

        if (content.notes.isNullOrEmpty().not()) {
            ItemDetailsEntry(
                title = MdtLocale.strings.wifiNotesLabel,
                subtitle = content.notes.orEmpty(),
                isCompact = true,
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("wifiContentCopyNotesButton"),
                        icon = MdtIcons.Copy,
                        onClick = { context.copyToClipboard(content.notes.orEmpty()) },
                    )
                },
            )
        }
    }

    if (content.supportConnect()) {
        Spacer(modifier = Modifier.height(16.dp))

        OptionEntry(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedShape12)
                .background(MdtTheme.color.surfaceContainer)
                .clickable(onClick = {
                    content.password?.let { password ->
                        if (decryptedFields[SecretFieldType.WifiConnectPassword] == null) {
                            onToggleSecretField(SecretFieldType.WifiConnectPassword, password)
                        }
                        showConnectWifi = true
                    }
                })
                .padding(horizontal = OptionEntryPaddingHorizontal),
            external = true,
            externalIcon = MdtIcons.Wifi4BarPlus,
            title = MdtLocale.strings.wifiConnectLabel,
            contentPadding = ZeroPadding,
        )
    }

    if (content.supportQrCodeContent()) {
        Spacer(modifier = Modifier.height(16.dp))

        QrCodeEntry(
            qrCodeContent = if (content.password == null) {
                content.qrCodeContent(null)
            } else {
                decryptedFields[SecretFieldType.WifiQrPassword]?.let {
                    content.qrCodeContent(it)
                }
            },
            expanded = showWifiQrCode,
            ssid = content.ssid,
            onClick = {
                if (showWifiQrCode) {
                    showWifiQrCode = false
                } else {
                    content.password?.let { password ->
                        if (decryptedFields[SecretFieldType.WifiQrPassword] == null) {
                            onToggleSecretField(SecretFieldType.WifiQrPassword, password)
                        }
                    }
                    showWifiQrCode = true
                    scope.launch {
                        onScrollToBottom()
                        val animationDuration = 300
                        val dt = 10
                        repeat(animationDuration / dt + 1) {
                            if (showWifiQrCode) {
                                delay(dt.toLong())
                                onScrollToBottom()
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun QrCodeEntry(
    ssid: String?,
    qrCodeContent: String?,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = OptionEntryPaddingHorizontal),
    ) {
        OptionEntry(
            external = true,
            externalIcon = if (expanded) MdtIcons.VisibilityOff else MdtIcons.Visibility,
            title = MdtLocale.strings.wifiShowQrCodeAction,
            contentPadding = ZeroPadding,
        )
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            visible = expanded,
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WifiQrCode(content = qrCodeContent)
                Text(
                    text = if (ssid == null) {
                        MdtLocale.strings.wifiQrScanTitle
                    } else {
                        MdtLocale.strings.wifiQrJoinTitle(ssid)
                    },
                    style = MdtTheme.typo.labelSmall,
                    color = MdtTheme.color.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WifiQrCode(content: String?) {
    Box(
        modifier = Modifier
            .size(176.dp)
            .clip(RoundedShape16)
            .background(QrCodeDefaults.colors().backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        content?.let {
            QrCode(
                content = content,
                size = 160.dp,
            )
        }
    }
}

@Preview
@Composable
private fun WifiContentPreview() {
    PreviewTheme {
        Column {
            WifiContent(
                item = WifiItemPreview,
                tags = persistentListOf(),
                WifiItemContentPreview.copy(hidden = true),
                decryptedFields = emptyMap(),
                onCopySecretField = { _, _ -> },
                onToggleSecretField = { _, _ -> },
                onScrollToBottom = {},
            )
        }
    }
}

@Preview
@Composable
private fun QrCodeEntryPreview() {
    PreviewTheme {
        QrCodeEntry(
            qrCodeContent = null,
            expanded = true,
            ssid = null,
            onClick = {},
        )
    }
}