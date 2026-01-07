package com.twofasapp.feature.settings.ui.autofill.browsers

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap

internal class BrowserAutofillManagerImpl(
    private val context: Context,
) : BrowserAutofillManager {

    override fun checkBrowsersStatus(): List<BrowserAutofillStatus> {
        return BrowserAutofillStatus.SupportedBrowsers.mapNotNull { browser ->
            val installedBrowser = context.checkBrowserInstallation(browser.packageName) ?: return@mapNotNull null

            browser.copy(
                icon = installedBrowser.icon,
                autofillEnabled = installedBrowser.autofillEnabled,
            )
        }
    }

    private class InstalledBrowser(
        val icon: Painter?,
        val autofillEnabled: Boolean,
    )

    private fun Context.checkBrowserInstallation(packageName: String): InstalledBrowser? {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            if (resolveInfo.isEmpty()) return null

            val drawable = packageManager.getApplicationIcon(packageName)
            val icon = BitmapPainter(drawable.toBitmap().asImageBitmap())

            InstalledBrowser(
                icon = icon,
                autofillEnabled = readThirdPartyAutofillState(packageName) ?: false,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun Context.readThirdPartyAutofillState(packageName: String): Boolean? {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(packageName + CONTENT_PROVIDER_NAME)
            .appendPath(THIRD_PARTY_MODE_ACTIONS_URI_PATH)
            .build()

        return try {
            contentResolver.query(
                uri,
                arrayOf(THIRD_PARTY_MODE_COLUMN),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return null
                val columnIndex = cursor.getColumnIndex(THIRD_PARTY_MODE_COLUMN)
                if (columnIndex == -1) return null
                val thirdPartyValue = cursor.getInt(columnIndex)
                thirdPartyValue != 0
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val CONTENT_PROVIDER_NAME = ".AutofillThirdPartyModeContentProvider"
        const val THIRD_PARTY_MODE_ACTIONS_URI_PATH = "autofill_third_party_mode"
        const val THIRD_PARTY_MODE_COLUMN = "autofill_third_party_state"
    }
}