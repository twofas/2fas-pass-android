/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.pushnotifications

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.ktx.openAppNotificationSettings
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.feature.permissions.PermissionStatus
import com.twofasapp.feature.permissions.rememberPermissionStatus
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun PushNotificationsScreen(
    viewModel: PushNotificationsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
    )
}

@Composable
private fun Content(
    uiState: PushNotificationsUiState,
) {
    val context = LocalContext.current
    var askForPermission by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    Scaffold(
        topBar = { TopAppBar(title = "Push Notifications") },
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
        ) {
            OptionEntry(
                title = null,
                subtitle = "Allow the browser extension to send you push notifications when you need to enter password data while browsing the web.",
                contentPadding = PaddingValues(horizontal = 16.dp),
            )

            Space(8.dp)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = rememberPermissionStatus(Manifest.permission.POST_NOTIFICATIONS)

                OptionSwitch(
                    title = "Allow notifications",
                    subtitle = "Allow 2FAS Pass browser extension notifications",
                    icon = MdtIcons.Notifications,
                    checked = when (permissionState.status) {
                        is PermissionStatus.Granted -> true
                        is PermissionStatus.Denied -> false
                    },
                    onToggle = {
                        when (permissionState.status) {
                            is PermissionStatus.Granted -> {
                                context.openAppNotificationSettings()
                            }

                            is PermissionStatus.Denied -> {
                                if ((permissionState.status as PermissionStatus.Denied).shouldShowRationale.not()) {
                                    context.openAppNotificationSettings()
                                } else {
                                    askForPermission = true
                                }
                            }
                        }
                    },
                )
            } else {
                OptionSwitch(
                    title = "Allow notifications",
                    subtitle = "Allow 2FAS Pass browser extension notifications",
                    icon = MdtIcons.Notifications,
                    checked = enabled,
                    onToggle = { context.openAppNotificationSettings() },
                )
            }
        }
    }

    if (askForPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            com.twofasapp.feature.permissions.RequestPermission(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onGranted = {},
                onDismissRequest = { askForPermission = false },
                rationaleEnabled = false,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = PushNotificationsUiState(),
        )
    }
}