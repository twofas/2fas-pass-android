/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.ktx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.BufferedReader

fun Context.readTextFile(fileUri: Uri, limitFileSize: Boolean = true): String {
    val fileDescriptor = contentResolver.openAssetFileDescriptor(fileUri, "r")
    val size = fileDescriptor?.length ?: 0

    if (size > 20 * 1024 * 1024 && limitFileSize) {
        throw RuntimeException("File size is too large. Maximum allowed size is 20MB.")
    }

    val inputStream = contentResolver.openInputStream(fileUri)!!
    val content = inputStream.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)

    fileDescriptor?.close()
    inputStream.close()

    return content
}

fun Context.readPdfAsBitmap(fileUri: Uri): Bitmap? {
    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var pdfRenderer: PdfRenderer? = null
    var bitmap: Bitmap? = null

    try {
        parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
        pdfRenderer = PdfRenderer(parcelFileDescriptor!!)

        val pageCount = pdfRenderer.pageCount
        if (pageCount > 0) {
            val page = pdfRenderer.openPage(0) // Get the first page
            val pageWidth = page.width
            val pageHeight = page.height

            bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }

    return bitmap
}