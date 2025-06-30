/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.import

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

data class ZipFile(
    val uri: Uri,
) {
    fun read(
        context: Context,
        filter: (String) -> Boolean,
    ): Map<String, String> {
        val fileContents = mutableMapOf<String, String>()
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType !in listOf("application/zip", "application/x-zip-compressed")) {
            throw RuntimeException("Invalid file type: $mimeType")
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var zipEntry = zis.nextEntry
                while (zipEntry != null) {
                    if (zipEntry.isDirectory.not() && zipEntry.name.startsWith("__").not()) {
                        if (filter(zipEntry.name.lowercase())) {
                            val baos = ByteArrayOutputStream()
                            val buffer = ByteArray(1024)
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                baos.write(buffer, 0, len)
                            }

                            fileContents[zipEntry.name] = String(baos.toByteArray(), charset("UTF-8"))
                        }
                    }
                    zis.closeEntry()
                    zipEntry = zis.nextEntry
                }
            }
        }

        return fileContents
    }
}