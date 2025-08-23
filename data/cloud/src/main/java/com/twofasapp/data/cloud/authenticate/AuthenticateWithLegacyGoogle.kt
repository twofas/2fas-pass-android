/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.cloud.authenticate

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.common.services.CrashlyticsInstance
import com.twofasapp.data.cloud.domain.CloudConfig

@Composable
internal fun AuthenticateWithLegacyGoogle(
    onDismissRequest: () -> Unit = {},
    onSuccess: (CloudConfig.GoogleDrive) -> Unit = {},
    onError: (Exception) -> Unit = {},
) {
    val activity = LocalContext.currentActivity

    val authorizationIntentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        .addOnSuccessListener {
                            if (it.grantedScopes.contains(Scope(DriveScopes.DRIVE_APPDATA))) {
                                onSuccess(
                                    CloudConfig.GoogleDrive(
                                        id = it.email.orEmpty(),
                                        credentialType = it.account?.type.orEmpty(),
                                    ),
                                )
                                onDismissRequest()
                            } else {
                                onDismissRequest()
                            }
                        }
                        .addOnCanceledListener { onDismissRequest() }
                        .addOnFailureListener { onError(it) }
                } else {
                    CrashlyticsInstance.logException(IllegalStateException("Legacy Authorization data is empty."))
                    onDismissRequest()
                }
            } else {
                CrashlyticsInstance.logException(IllegalStateException("Legacy Authorization result failed."))
                onDismissRequest()
            }
        }

    LaunchedEffect(Unit) {
        val signInOptions = GoogleSignInOptions.Builder()
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        val client = GoogleSignIn.getClient(activity, signInOptions)

        client.signInIntent

        authorizationIntentLauncher.launch(client.signInIntent)
    }
}