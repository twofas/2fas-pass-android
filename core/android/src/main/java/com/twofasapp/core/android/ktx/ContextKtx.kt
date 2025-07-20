/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.android.ktx

import android.app.Activity
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.UriHandler
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

val CompositionLocal<Context>.currentActivity: AppCompatActivity
    @Composable
    get() {
        var context = this.current

        while (context is ContextWrapper) {
            if (context is AppCompatActivity) return context
            context = context.baseContext
        }
        if (LocalInspectionMode.current) {
            // Dummy object in edit mode
            return AppCompatActivity()
        } else {
            error("No component activity")
        }
    }

val Context.settingsIntent: Intent
    get() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

fun Context.openAppNotificationSettings() {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }

            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.fromParts("package", packageName, null)
            }
        }
    }

    startActivity(intent)
}

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val LocalBackDispatcher
    @Composable
    get() = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

fun Context.toast(message: String, duration: Int) {
    try {
        Toast.makeText(this, message, duration).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.toast(@StringRes resId: Int, duration: Int) {
    try {
        Toast.makeText(this, this.resources.getText(resId), duration).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.toastLong(message: String) = toast(message, Toast.LENGTH_LONG)
fun Context.toastLong(@StringRes resId: Int) = toast(resId, Toast.LENGTH_LONG)

fun Context.toastShort(message: String) = toast(message, Toast.LENGTH_SHORT)
fun Context.toastShort(@StringRes resId: Int) = toast(resId, Toast.LENGTH_SHORT)

fun Context.copyToClipboard(
    text: String,
    label: String = "Text",
    toast: String = "Copied!",
    isSensitive: Boolean = false,
    showToast: Boolean = true,
) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text).apply {
        if (isSensitive) {
            description.extras = PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                } else {
                    putBoolean("android.content.extra.IS_SENSITIVE", true)
                }
            }
        }
    }
    clipboardManager.setPrimaryClip(clipData)

    val manufacturer = Build.MANUFACTURER.lowercase()
    val shouldShowToast = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 ||
        manufacturer.contains("xiaomi") ||
        manufacturer.contains("redmi")

    if (shouldShowToast && showToast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show()
    }
}

fun UriHandler.openSafely(uri: String?, context: Context? = null) {
    try {
        uri?.let {
            // This needs to be refactored at some point
            if (uri.startsWith(uriPrefixAndroidApp, true)) {
                val launchIntent = context?.packageManager?.getLaunchIntentForPackage(uri.replace(uriPrefixAndroidApp, ""))
                launchIntent?.let { app -> context.startActivity(app) }
            } else if (uri.startsWith("http://", true) || uri.startsWith("https://", true)) {
                openUri(it)
            } else {
                openUri("$uriPrefixWebsite$it")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        context?.let { Toast.makeText(it, "No application that supports this link", Toast.LENGTH_SHORT).show() }
    }
}

fun Color.toRgbHex(): String {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format("#%02x%02x%02x", red.toInt(), green.toInt(), blue.toInt())
}

fun String?.hexToColor(): Color {
    return if (this == null) Color.Unspecified else Color(android.graphics.Color.parseColor(this))
}

fun Context.clearTmpDir() {
    val tmpDir = File(getExternalFilesDir(null), "tmp")
    tmpDir.deleteRecursively()
}

fun Context.showShareFilePicker(
    filename: String,
    title: String,
    save: (OutputStream) -> Unit,
) {
    val tmpDir = File(getExternalFilesDir(null), "tmp")

    tmpDir.mkdir()

    val file = File(tmpDir, filename)
    val outputStream = FileOutputStream(file)

    save(outputStream)
    outputStream.close()

    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

    val shareIntent = Intent().apply {
        type = "*/*"
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, filename)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(
        Intent.createChooser(
            shareIntent,
            title,
        ),
    )
}

fun Context.restartApp() {
    val packageManager: PackageManager = packageManager
    val intent: Intent = packageManager.getLaunchIntentForPackage(packageName)!!
    val componentName: ComponentName = intent.component!!
    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
    startActivity(restartIntent)
    Runtime.getRuntime().exit(0)
}

fun Activity.makeWindowSecure(allow: Boolean) {
    if (allow) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
    }
}

val uriPrefixAndroidApp = "androidapp://"
val uriPrefixWebsite = "https://"

inline fun <reified T> Bundle?.getSafelyParcelable(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.classLoader = T::class.java.classLoader
        this?.getParcelable(key, T::class.java) ?: try {
            @Suppress("DEPRECATION")
            this?.getParcelable(key)
        } catch (e: Exception) {
            null
        }
    } else {
        @Suppress("DEPRECATION")
        this?.getParcelable(key)
    }
}