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
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.drive.DriveScopes
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.common.build.LocalConfig
import com.twofasapp.data.cloud.domain.CloudConfig
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
internal fun AuthenticateWithGoogle(
    localConfig: LocalConfig = koinInject(),
    onDismissRequest: () -> Unit = {},
    onSuccess: (CloudConfig.GoogleDrive) -> Unit = {},
    onError: (Exception) -> Unit = {},
) {
    val activity = LocalContext.currentActivity
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(activity) }
    var googleDriveConfig: CloudConfig.GoogleDrive? by remember { mutableStateOf(null) }
    var authenticated by remember { mutableStateOf(false) }

    DisposableEffect(authenticated) {
        onDispose {
            scope.launch {
                googleDriveConfig?.let(onSuccess)
                onDismissRequest()
            }
        }
    }

    val authorizationIntentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val token = Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(result.data).accessToken

                    if (token != null) {
                        authenticated = true
                    } else {
                        onDismissRequest()
                    }
                } else {
                    onDismissRequest()
                }
            } else {
                onDismissRequest()
            }
        }

    val googleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(localConfig.googleAuthClientId)
            .build()
    }

    val credentialRequest = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Sign In with Google in order to get Google account credentials (token, id etc.)
                val credentialResponse = credentialManager.getCredential(
                    request = credentialRequest,
                    context = activity,
                )

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
                googleDriveConfig = CloudConfig.GoogleDrive(
                    id = googleIdTokenCredential.id,
                    credentialType = googleIdTokenCredential.type,
                )

                // Request authorization (permission to use Google Drive for selected Google account)
                val authorizationRequest = AuthorizationRequest.builder()
                    .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
                    .build()

                Identity.getAuthorizationClient(activity)
                    .authorize(authorizationRequest)
                    .addOnSuccessListener { authorizationResult ->
                        if (authorizationResult.hasResolution()) {
                            // User need to grant permission
                            authorizationResult.pendingIntent?.let { pendingIntent ->
                                authorizationIntentLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                            }
                        } else {
                            // Permission already granted
                            authenticated = true
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        onError(e)
                        onDismissRequest()
                    }
            } catch (e: Exception) {
                e.printStackTrace()

                if (e is GetCredentialCancellationException) {
                    onDismissRequest()
                } else {
                    onError(e)
                    onDismissRequest()
                }
            }
        }
    }
}