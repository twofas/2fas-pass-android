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
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ZipFile(
    val uri: Uri,
) {
    /**
     * Reads text files from a ZIP archive.
     *
     * This method attempts to read ZIP entries using ZipInputStream for efficiency.
     * If it encounters STORED entries with unknown size (which ZipInputStream cannot handle),
     * it falls back to using a temporary file with java.util.zip.ZipFile for proper random access.
     *
     * @param context Android context for accessing content resolver and cache directory
     * @param filter Predicate to filter which entries to read (applied to lowercase entry name)
     * @return Map of entry names to their text content (UTF-8)
     * @throws RuntimeException if the file is not a valid ZIP file
     */
    fun read(
        context: Context,
        filter: (String) -> Boolean = { true },
    ): Map<String, String> {
        val mimeType = context.contentResolver.getType(uri)

        if (mimeType !in listOf("application/zip", "application/x-zip-compressed")) {
            throw RuntimeException("Invalid file type: $mimeType")
        }

        // First pass: Try reading with ZipInputStream (efficient, no temp file)
        val (fileContents, hasProblematicEntries) = tryReadWithZipInputStream(context, filter)

        // If we found STORED entries with unknown size, use temp file approach
        return if (hasProblematicEntries) {
            readWithTempFile(context, filter)
        } else {
            fileContents
        }
    }

    /**
     * Attempts to read ZIP entries using ZipInputStream.
     *
     * @return Pair of (file contents map, whether problematic entries were found)
     */
    private fun tryReadWithZipInputStream(
        context: Context,
        filter: (String) -> Boolean,
    ): Pair<Map<String, String>, Boolean> {
        val fileContents = mutableMapOf<String, String>()
        var hasProblematicEntries = false

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var zipEntry = zis.nextEntry

                while (zipEntry != null) {
                    // Skip directories and system files
                    if (zipEntry.isDirectory || zipEntry.name.startsWith("__")) {
                        zis.closeEntry()
                        zipEntry = zis.nextEntry
                        continue
                    }

                    // Apply filter to lowercase entry name
                    if (!filter(zipEntry.name.lowercase())) {
                        zis.closeEntry()
                        zipEntry = zis.nextEntry
                        continue
                    }

                    // Check if this is a STORED entry with unknown size
                    if (zipEntry.method == ZipEntry.STORED && zipEntry.size == -1L) {
                        // Cannot read with ZipInputStream, need temp file approach
                        hasProblematicEntries = true
                        break
                    }

                    // Read entry content normally
                    val baos = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (zis.read(buffer).also { bytesRead = it } != -1) {
                        baos.write(buffer, 0, bytesRead)
                    }

                    val content = String(baos.toByteArray(), charset("UTF-8"))
                    fileContents[zipEntry.name] = content

                    zis.closeEntry()
                    zipEntry = zis.nextEntry
                }
            }
        }

        return Pair(fileContents, hasProblematicEntries)
    }

    /**
     * Reads ZIP entries using a temporary file for random access.
     * Required for STORED entries with data descriptors (e.g., Proton Pass exports).
     */
    private fun readWithTempFile(
        context: Context,
        filter: (String) -> Boolean,
    ): Map<String, String> {
        val fileContents = mutableMapOf<String, String>()
        val tempFile = File.createTempFile("zip_import_", ".tmp", context.cacheDir)

        try {
            try {
                // Copy URI content to temporary file
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Read ZIP entries using java.util.zip.ZipFile for proper handling
                java.util.zip.ZipFile(tempFile).use { zipFile ->
                    val entries = zipFile.entries()

                    while (entries.hasMoreElements()) {
                        val zipEntry = entries.nextElement()

                        // Skip directories and system files
                        if (zipEntry.isDirectory || zipEntry.name.startsWith("__")) {
                            continue
                        }

                        // Apply filter to lowercase entry name
                        if (!filter(zipEntry.name.lowercase())) {
                            continue
                        }

                        // Read and store entry content
                        zipFile.getInputStream(zipEntry).use { entryStream ->
                            val bytes = entryStream.readBytes()
                            val content = String(bytes, charset("UTF-8"))
                            fileContents[zipEntry.name] = content
                        }
                    }
                }
            } finally {
                if (tempFile.exists()) {
                    val deleted = tempFile.delete()
                    if (!deleted) {
                        tempFile.deleteOnExit()
                    }
                }
            }
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e
        }

        return fileContents
    }
}