package com.twofasapp.feature.cloudsync.ui.googledrive

import androidx.compose.runtime.Composable
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.foundation.dialog.InfoDialog
import com.twofasapp.core.locale.MdtLocale

@Composable
fun GoogleDriveInfoDialog(
    onDismissRequest: () -> Unit,
) {
    InfoDialog(
        title = MdtLocale.strings.settingsEntryGoogleDrive,
        body = MdtLocale.strings.settingsEntryGoogleDriveSyncExplanation,
        icon = MdtIcons.Drive,
        onDismissRequest = onDismissRequest,
    )
}