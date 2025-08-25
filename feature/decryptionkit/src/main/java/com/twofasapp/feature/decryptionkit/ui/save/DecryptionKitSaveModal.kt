/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.decryptionkit.ui.save

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.twofasapp.core.android.ktx.isRunningCustomOs
import com.twofasapp.core.android.ktx.showShareFilePicker
import com.twofasapp.core.android.ktx.toastShort
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.locale.MdtLocale
import com.twofasapp.feature.decryptionkit.generator.DecryptionKit
import com.twofasapp.feature.decryptionkit.generator.DecryptionKitGenerator
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun DecryptionKitSaveModal(
    onDismissRequest: () -> Unit,
    decryptionKit: DecryptionKit,
    includeMasterKey: Boolean,
    onFileSaved: () -> Unit = {},
    onShowConfirmation: () -> Unit = {},
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        PDFBoxResourceLoader.init(context)
    }

    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.decryptionKitSaveModalTitle,
    ) { dismiss ->
        Content(
            decryptionKit = decryptionKit,
            includeMasterKey = includeMasterKey,
            onFileSaved = { dismiss { onFileSaved() } },
            onShowConfirmation = { dismiss { onShowConfirmation() } },
        )
    }
}

@Composable
private fun Content(
    decryptionKit: DecryptionKit,
    includeMasterKey: Boolean,
    onFileSaved: () -> Unit = {},
    onShowConfirmation: () -> Unit = {},
) {
    val context = LocalContext.current
    val strings = MdtLocale.strings
    val scope = rememberCoroutineScope()
    val isRunningCustomOs = remember { isRunningCustomOs() }

    var shareClicked by remember { mutableStateOf(false) }
    var saveClicked by remember { mutableStateOf(false) }

    val directoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        scope.launch {
            uri?.let { fileUri ->
                DecryptionKitGenerator.generate(
                    context = context,
                    fileUri = fileUri,
                    kit = decryptionKit,
                    includeMasterKey = includeMasterKey,
                )

                context.toastShort(strings.decryptionKitSaveToast)

                if (isRunningCustomOs.not()) {
                    onFileSaved()
                } else {
                    saveClicked = false
                    onShowConfirmation()
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (shareClicked) {
            shareClicked = false
            onShowConfirmation()
            return@LifecycleEventEffect
        }

        if (saveClicked && isRunningCustomOs) {
            saveClicked = false
            onShowConfirmation()
            return@LifecycleEventEffect
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Text(
            text = strings.decryptionKitSaveModalDescription,
            style = MdtTheme.typo.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        OptionEntry(
            title = strings.decryptionKitSaveModalCta1,
            icon = MdtIcons.Share,
            onClick = {
                shareClicked = true

                context.showShareFilePicker(
                    filename = DecryptionKitGenerator.generateFilename(),
                    title = "2FAS Pass Decryption Kit",
                    save = { outputStream ->
                        runBlocking {
                            outputStream.use {
                                DecryptionKitGenerator.generate(
                                    context = context,
                                    outputStream = it,
                                    kit = decryptionKit,
                                    includeMasterKey = includeMasterKey,
                                )
                            }
                        }
                    },
                )
            },
        )

        OptionEntry(
            title = strings.decryptionKitSaveModalCta2,
            icon = MdtIcons.Save,
            onClick = {
                saveClicked = true

                directoryPicker.launch(DecryptionKitGenerator.generateFilename())
            },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            decryptionKit = DecryptionKit.Empty,
            includeMasterKey = true,
        )
    }
}