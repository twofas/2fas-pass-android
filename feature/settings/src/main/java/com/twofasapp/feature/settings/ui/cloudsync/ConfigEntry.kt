/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2026 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.cloudsync

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.twofasapp.core.android.ktx.copyToClipboard
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.button.ButtonStyle
import com.twofasapp.core.design.foundation.button.IconButton
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.design.foundation.menu.DropdownMenu
import com.twofasapp.core.design.foundation.menu.DropdownMenuItem
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.data.cloud.domain.CloudConfig
import com.twofasapp.data.cloud.domain.CloudConnection
import com.twofasapp.data.cloud.domain.CloudSyncStatus
import com.twofasapp.data.cloud.exceptions.CloudError
import com.twofasapp.data.cloud.exceptions.asCode
import com.twofasapp.data.cloud.exceptions.asMessage
import com.twofasapp.feature.cloudsync.ui.common.formatCloudErrorDetails
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ConfigEntry(
    config: CloudConfig,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onChangePassword: () -> Unit,
    onReplaceBackup: () -> Unit,
) {
    val errorStatus = config.status as? CloudSyncStatus.Error

    Column(
        modifier = Modifier
            .padding(horizontal = ScreenPadding)
            .fillMaxWidth()
            .clip(RoundedShape12)
            .background(MdtTheme.color.surfaceContainer)
            .padding(16.dp),
    ) {
        ConfigHeader(
            config = config,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
        )

        Space(12.dp)

        SyncStatusRow(config = config)

        if (errorStatus != null) {
            Space(12.dp)

            ErrorActions(
                error = errorStatus.error,
                onChangePassword = onChangePassword,
                onReplaceBackup = onReplaceBackup,
            )
        }
    }
}

@Composable
private fun ErrorActions(
    error: CloudError,
    onChangePassword: () -> Unit,
    onReplaceBackup: () -> Unit,
) {
    val strings = MdtLocale.strings
    val uriHandler = LocalUriHandler.current

    when (error) {
        is CloudError.WrongBackupPassword -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    text = strings.cloudSyncActionReplaceBackup,
                    modifier = Modifier.weight(1f),
                    height = 40.dp,
                    onClick = onReplaceBackup,
                )

                Button(
                    text = strings.cloudSyncActionChangePassword,
                    modifier = Modifier.weight(1f),
                    height = 40.dp,
                    onClick = onChangePassword,
                )
            }
        }

        is CloudError.InvalidSchemaVersion -> {
            Button(
                text = strings.cloudSyncInvalidSchemaErrorCta,
                modifier = Modifier.fillMaxWidth(),
                height = 40.dp,
                onClick = { uriHandler.openSafely(MdtLocale.links.playStore) },
            )
        }

        else -> {
            ErrorDetails(error = error)
        }
    }
}

@Composable
private fun ErrorDetails(error: CloudError) {
    val cause = error.cause ?: return
    val context = LocalContext.current
    val strings = MdtLocale.strings
    var showDialog by remember { mutableStateOf(false) }

    val details = buildString {
        append(error.asMessage())
        append("\n\n")
        append(cause.formatCloudErrorDetails())
    }

    Button(
        text = strings.cloudSyncShowErrorDetails,
        style = ButtonStyle.Text,
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDialog = true },
    )

    if (showDialog) {
        InfoDialog(
            onDismissRequest = { showDialog = false },
            title = "Sync failed! (Error ${error.asCode()})",
            bodyAnnotated = AnnotatedString(
                text = details,
                spanStyle = SpanStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
            ),
            positive = strings.commonOk,
            negative = strings.commonCopy,
            onNegative = { context.copyToClipboard(details) },
        )
    }
}

@Composable
private fun ConfigHeader(
    config: CloudConfig,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val strings = MdtLocale.strings
    val (title, addressLabel) = when (val spec = config.connection) {
        is CloudConnection.GoogleDrive -> strings.settingsEntryGoogleDrive to spec.accountId
        is CloudConnection.WebDav -> spec.url.toUri().host.toString() to spec.username
        is CloudConnection.S3 -> spec.endpoint.toUri().host.toString() to spec.bucket
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ProviderIcon(connection = config.connection)

        Space(16.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MdtTheme.typo.titleMedium,
                color = MdtTheme.color.onSurface,
            )

            if (addressLabel.isNotBlank()) {
                Text(
                    text = addressLabel,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurfaceVariant,
                )
            }
        }

        ConfigEntryMenu(
            connection = config.connection,
            loading = config.status is CloudSyncStatus.Syncing,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
        )
    }
}

@Composable
private fun ProviderIcon(connection: CloudConnection) {
    val size = 24.dp
    when (connection) {
        is CloudConnection.GoogleDrive -> Image(
            painter = painterResource(com.twofasapp.core.design.R.drawable.external_logo_googledrive),
            contentDescription = null,
            modifier = Modifier.size(size),
        )

        is CloudConnection.WebDav -> Icon(
            painter = MdtIcons.Lan,
            contentDescription = null,
            tint = MdtTheme.color.primary,
            modifier = Modifier.size(size),
        )

        is CloudConnection.S3 -> Icon(
            painter = MdtIcons.Bucket,
            contentDescription = null,
            tint = MdtTheme.color.primary,
            modifier = Modifier.size(size),
        )
    }
}

private enum class SyncStatusUi {
    Syncing,
    Error,
    NotSynced,
    Synced,
}

@Composable
private fun SyncStatusRow(config: CloudConfig) {
    val status = config.status
    val state = when {
        status is CloudSyncStatus.Syncing -> SyncStatusUi.Syncing
        status is CloudSyncStatus.Error -> SyncStatusUi.Error
        config.syncedAt == 0L -> SyncStatusUi.NotSynced
        else -> SyncStatusUi.Synced
    }

    Crossfade(
        modifier = Modifier.fillMaxWidth(),
        targetState = state,
        animationSpec = tween(300),
        label = "SyncStatusRow",
    ) { current ->
        when (current) {
            SyncStatusUi.Syncing -> SyncingStatus()
            SyncStatusUi.Error -> ErrorStatus(status = config.status)
            SyncStatusUi.NotSynced -> NotSyncedStatus()
            SyncStatusUi.Synced -> SyncedStatus(syncedAt = config.syncedAt)
        }
    }
}

@Composable
private fun SyncStatusRowLayout(
    tint: Color,
    label: String,
    icon: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Space(3.dp)

        icon()

        Space(8.dp)

        Text(
            text = label,
            style = MdtTheme.typo.medium.sm,
            color = tint,
        )
    }
}

@Composable
private fun SyncingStatus() {
    val tint = MdtTheme.color.primary
    SyncStatusRowLayout(
        tint = tint,
        label = "Syncing…",
        icon = {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = tint,
                strokeWidth = 2.dp,
            )
        },
    )
}

@Composable
private fun ErrorStatus(status: CloudSyncStatus) {
    val tint = MdtTheme.color.error
    val code = (status as? CloudSyncStatus.Error)?.error?.asCode()
    SyncStatusRowLayout(
        tint = tint,
        label = "Sync failed! (Error $code)",
        icon = {
            Icon(
                painter = MdtIcons.Error,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        },
    )
}

@Composable
private fun NotSyncedStatus() {
    val tint = MdtTheme.color.onSurfaceVariant
    SyncStatusRowLayout(
        tint = tint,
        label = "Not synced yet",
        icon = {
            Icon(
                painter = MdtIcons.CloudSync,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        },
    )
}

@Composable
private fun SyncedStatus(syncedAt: Long) {
    val strings = MdtLocale.strings
    val tint = MdtTheme.color.primary

    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000.milliseconds)
            tick++
        }
    }

    tick
    SyncStatusRowLayout(
        tint = tint,
        label = "Synced ${strings.formatDuration(syncedAt)}",
        icon = {
            Icon(
                painter = MdtIcons.CircleCheckFilled,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        },
    )
}

@Composable
private fun ConfigEntryMenu(
    connection: CloudConnection,
    loading: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showDropdown by remember { mutableStateOf(false) }

    DropdownMenu(
        visible = showDropdown,
        onDismissRequest = { showDropdown = false },
        anchor = {
            IconButton(
                icon = MdtIcons.More,
                iconTint = MdtTheme.color.outline,
                enabled = loading.not(),
                modifier = Modifier.offset(x = 8.dp).testTag("cloudConfigMenuButton"),
                onClick = { showDropdown = true },
            )
        },
        content = {
            when (connection) {
                is CloudConnection.GoogleDrive -> {
                    DropdownMenuItem(
                        text = MdtLocale.strings.commonInfo,
                        leadingIcon = MdtIcons.Info,
                        onClick = {
                            showDropdown = false
                            onEditClick()
                        },
                    )
                }

                is CloudConnection.S3 -> {
                    DropdownMenuItem(
                        text = MdtLocale.strings.commonEdit,
                        leadingIcon = MdtIcons.Edit,
                        onClick = {
                            showDropdown = false
                            onEditClick()
                        },
                    )
                }

                is CloudConnection.WebDav -> {
                    DropdownMenuItem(
                        text = MdtLocale.strings.commonEdit,
                        leadingIcon = MdtIcons.Edit,
                        onClick = {
                            showDropdown = false
                            onEditClick()
                        },
                    )
                }
            }

            DropdownMenuItem(
                text = MdtLocale.strings.commonDelete,
                leadingIcon = MdtIcons.Delete,
                contentColor = MdtTheme.color.error,
                onClick = {
                    showDropdown = false
                    onDeleteClick()
                },
            )
        },
    )
}

private fun previewGoogleDrive(
    status: CloudSyncStatus = CloudSyncStatus.Synced,
    syncedAt: Long = System.currentTimeMillis() - 120_000,
) = CloudConfig(
    id = "gdrive",
    syncedAt = syncedAt,
    status = status,
    connection = CloudConnection.GoogleDrive(
        accountId = "rafal@midnite.com",
        credentialType = "",
    ),
)

private fun previewWebDav(
    status: CloudSyncStatus = CloudSyncStatus.Synced,
    syncedAt: Long = System.currentTimeMillis() - 600_000,
) = CloudConfig(
    id = "webdav",
    syncedAt = syncedAt,
    status = status,
    connection = CloudConnection.WebDav(
        url = "https://cloud.example.com/dav",
        username = "user",
        password = "",
        allowUntrustedCertificate = false,
    ),
)

private fun previewS3(
    status: CloudSyncStatus = CloudSyncStatus.Synced,
    syncedAt: Long = System.currentTimeMillis() - 60_000,
) = CloudConfig(
    id = "s3",
    syncedAt = syncedAt,
    status = status,
    connection = CloudConnection.S3(
        endpoint = "https://s3.amazonaws.com",
        region = "us-east-1",
        bucket = "my-bucket",
        accessKeyId = "",
        secretAccessKey = "",
        allowUntrustedCertificate = false,
    ),
)

@Preview
@Composable
private fun PreviewSynced() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            ConfigEntry(previewGoogleDrive(), {}, {}, {}, {})
            ConfigEntry(previewWebDav(), {}, {}, {}, {})
            ConfigEntry(previewS3(), {}, {}, {}, {})
        }
    }
}

@Preview
@Composable
private fun PreviewNotSyncedYet() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            ConfigEntry(previewGoogleDrive(syncedAt = 0L, status = CloudSyncStatus.Idle), {}, {}, {}, {})
            ConfigEntry(previewWebDav(syncedAt = 0L, status = CloudSyncStatus.Idle), {}, {}, {}, {})
            ConfigEntry(previewS3(syncedAt = 0L, status = CloudSyncStatus.Idle), {}, {}, {}, {})
        }
    }
}

@Preview
@Composable
private fun PreviewSyncing() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            ConfigEntry(previewGoogleDrive(status = CloudSyncStatus.Syncing), {}, {}, {}, {})
            ConfigEntry(previewWebDav(status = CloudSyncStatus.Syncing), {}, {}, {}, {})
            ConfigEntry(previewS3(status = CloudSyncStatus.Syncing), {}, {}, {}, {})
        }
    }
}

@Preview
@Composable
private fun PreviewErrorWrongPassword() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            ConfigEntry(
                config = previewGoogleDrive(status = CloudSyncStatus.Error(CloudError.WrongBackupPassword(null))),
                onEditClick = {},
                onDeleteClick = {},
                onChangePassword = {},
                onReplaceBackup = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewErrorNoNetwork() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            ConfigEntry(
                config = previewWebDav(
                    status = CloudSyncStatus.Error(
                        CloudError.NoNetwork(RuntimeException("Unable to resolve host")),
                    ),
                ),
                onEditClick = {},
                onDeleteClick = {},
                onChangePassword = {},
                onReplaceBackup = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewErrorInvalidSchema() {
    PreviewTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            ConfigEntry(
                config = previewS3(
                    status = CloudSyncStatus.Error(
                        CloudError.InvalidSchemaVersion(
                            cause = null,
                            backupSchemaVersion = 99,
                            supportedSchemaVersion = 1,
                        ),
                    ),
                ),
                onEditClick = {},
                onDeleteClick = {},
                onChangePassword = {},
                onReplaceBackup = {},
            )
        }
    }
}