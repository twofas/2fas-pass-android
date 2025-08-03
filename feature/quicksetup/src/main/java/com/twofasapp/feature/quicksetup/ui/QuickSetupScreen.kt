package com.twofasapp.feature.quicksetup.ui

import android.view.autofill.AutofillManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonHeight
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.checked.Switch
import com.twofasapp.core.design.foundation.layout.ActionsRow
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.TextIcon
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel

@Composable
fun QuickSetupRoute(
    openAutofill: () -> Unit,
    openSync: (syncEnabled: Boolean) -> Unit,
    openSecurityType: () -> Unit,
    openImport: () -> Unit,
    openTransfer: () -> Unit,
    close: () -> Unit,
) {
    QuickSetupScreen(
        openAutofill = openAutofill,
        openSync = openSync,
        openSecurityType = openSecurityType,
        openImport = openImport,
        openTransfer = openTransfer,
        close = close,
    )
}

@Composable
private fun QuickSetupScreen(
    viewModel: QuickSetupViewModel = koinViewModel(),
    openAutofill: () -> Unit = {},
    openSync: (syncEnabled: Boolean) -> Unit = {},
    openSecurityType: () -> Unit = {},
    openImport: () -> Unit = {},
    openTransfer: () -> Unit = {},
    close: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val autofillManager: AutofillManager = activity!!.getSystemService(AutofillManager::class.java)
    var autofillServiceEnabled: Boolean by remember { mutableStateOf(autofillManager.hasEnabledAutofillServices()) }

    Content(
        uiState = uiState,
        autofillEnabled = autofillServiceEnabled,
        onAutofillClick = openAutofill,
        onSyncClick = openSync,
        onSecurityTypeClick = openSecurityType,
        onImportClick = openImport,
        onTransferClick = openTransfer,
        onCloseClick = { viewModel.markAsPrompted { close() } },
    )
}

@Composable
private fun Content(
    uiState: QuickSetupUiState,
    autofillEnabled: Boolean = false,
    onAutofillClick: () -> Unit = {},
    onSyncClick: (syncEnabled: Boolean) -> Unit = {},
    onSecurityTypeClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    val strings = MdtLocale.strings

    BackHandler {
        onCloseClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                showBackButton = false,
                actions = {
                    ActionsRow {
                        IconButton(
                            icon = MdtIcons.Close,
                            iconTint = MdtTheme.color.onBackground,
                            onClick = onCloseClick,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .background(MdtTheme.color.background)
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(com.twofasapp.core.design.R.drawable.quick_setup_settings),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                )

                Space(16.dp)

                Text(
                    text = strings.quickSetupTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MdtTheme.typo.headlineSmall,
                )

                Space(8.dp)

                Text(
                    text = strings.quickSetupDescription,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenPadding),
                    textAlign = TextAlign.Center,
                )

                Space(24.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = ScreenPadding)
                        .clip(RoundedShape12)
                        .background(MdtTheme.color.surfaceContainer)
                        .clickable { onAutofillClick() }
                        .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        ItemTitle(
                            text = strings.quickSetupAutofillTitle,
                            recommended = true,
                        )

                        Space(8.dp)

                        Text(
                            text = strings.quickSetupAutofillDescription,
                            style = MdtTheme.typo.regular.sm,
                            color = MdtTheme.color.onSurfaceVariant,
                        )
                    }

                    Space(12.dp)

                    Switch(
                        checked = autofillEnabled,
                        onCheckedChange = { onAutofillClick() },
                    )
                }

                Space(8.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = ScreenPadding)
                        .clip(RoundedShape12)
                        .background(MdtTheme.color.surfaceContainer)
                        .clickable { onSyncClick(uiState.syncEnabled) }
                        .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        ItemTitle(
                            text = strings.quickSetupSyncTitle,
                            recommended = true,
                        )

                        Space(8.dp)

                        Text(
                            text = strings.quickSetupSyncDescription,
                            style = MdtTheme.typo.regular.sm,
                            color = MdtTheme.color.onSurfaceVariant,
                        )
                    }

                    Space(12.dp)

                    Switch(
                        checked = uiState.syncEnabled,
                        onCheckedChange = { onSyncClick(uiState.syncEnabled) },
                    )
                }

                Space(8.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = ScreenPadding)
                        .clip(RoundedShape12)
                        .background(MdtTheme.color.surfaceContainer)
                        .clickable { onSecurityTypeClick() }
                        .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        ItemTitle(
                            text = strings.quickSetupSecurityTierTitle,
                            recommended = false,
                        )

                        Space(8.dp)

                        Text(
                            text = strings.quickSetupSecurityTierDescription,
                            style = MdtTheme.typo.regular.sm,
                            color = MdtTheme.color.onSurfaceVariant,
                        )

                        HorizontalDivider(
                            color = MdtTheme.color.surfaceContainerHigh,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = strings.quickSetupSecurityTierDefault,
                                style = MdtTheme.typo.bodyLarge,
                            )

                            Space(1f)

                            TextIcon(
                                text = when (uiState.securityType) {
                                    SecurityType.Tier1 -> strings.settingsEntrySecurityTier1
                                    SecurityType.Tier2 -> strings.settingsEntrySecurityTier2
                                    SecurityType.Tier3 -> strings.settingsEntrySecurityTier3
                                },
                                style = MdtTheme.typo.bodyLarge,
                                color = MdtTheme.color.onSurfaceVariant,
                                leadingIcon = when (uiState.securityType) {
                                    SecurityType.Tier3 -> MdtIcons.Tier3
                                    SecurityType.Tier2 -> MdtIcons.Tier2
                                    SecurityType.Tier1 -> MdtIcons.Tier1
                                },
                                leadingIconTint = MdtTheme.color.primary,
                                leadingIconSize = 20.dp,
                                leadingIconSpacer = 8.dp,
                            )

                            Space(8.dp)

                            Icon(
                                painter = MdtIcons.ChevronRight,
                                contentDescription = null,
                                tint = MdtTheme.color.onSurfaceVariant,
                                modifier = Modifier.offset(x = 4.dp),
                            )
                        }
                    }
                }

                Space(24.dp)

                Button(
                    text = strings.quickSetupImportItemsCta,
                    style = ButtonStyle.Text,
                    size = ButtonHeight.Small,
                    onClick = onImportClick,
                    leadingIcon = MdtIcons.ImportExport,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MdtTheme.color.surfaceContainerLow),

                )

                Space(8.dp)

                Button(
                    text = strings.quickSetupTransferItemsCta,
                    style = ButtonStyle.Text,
                    size = ButtonHeight.Small,
                    onClick = onTransferClick,
                    leadingIcon = MdtIcons.SyncAlt,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MdtTheme.color.surfaceContainerLow),
                )

                Space(12.dp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding)
                    .padding(bottom = ScreenPadding, top = 8.dp),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = strings.commonClose,
                    onClick = { onCloseClick() },
                )
            }
        }
    }
}

@Composable
private fun ItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    recommended: Boolean,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MdtTheme.typo.titleMedium,
        )

        if (recommended) {
            Space(8.dp)

            RecommendedLabel()
        }
    }
}

@Composable
private fun RecommendedLabel(modifier: Modifier = Modifier) {
    TextIcon(
        text = MdtLocale.strings.quickSetupRecommended,
        style = MdtTheme.typo.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, lineHeight = 10.sp),
        color = MdtTheme.color.onPrimaryContainer,
        leadingIcon = MdtIcons.Info,
        leadingIconTint = MdtTheme.color.onPrimaryContainer,
        leadingIconSize = 12.dp,
        modifier = modifier
            .clip(RoundedShape12)
            .background(MdtTheme.color.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun PreviewDark() {
    PreviewTheme(
        appTheme = AppTheme.Dark,
    ) {
        Content(
            uiState = QuickSetupUiState(),
        )
    }
}

@Preview
@Composable
private fun PreviewLight() {
    PreviewTheme(
        appTheme = AppTheme.Light,
    ) {
        Content(
            uiState = QuickSetupUiState(),
        )
    }
}