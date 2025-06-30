/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.qrscan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ReadQrFromImage(private val context: Context) {
    suspend operator fun invoke(uri: Uri): Result<String> {
        return invoke(InputImage.fromFilePath(context, uri))
    }

    suspend operator fun invoke(bitmap: Bitmap): Result<String> {
        return invoke(InputImage.fromBitmap(bitmap, 0))
    }

    private suspend operator fun invoke(image: InputImage): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                    ).build(),
            )
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val barcode = barcodes.getOrNull(0)
                    barcode?.rawValue?.let { text ->
                        continuation.resume(Result.success(text))
                    } ?: continuation.resume(Result.failure(RuntimeException()))
                }
                .addOnFailureListener {
                    continuation.resume(Result.failure(it))
                }
        } catch (e: Exception) {
            e.printStackTrace()

            continuation.resume(Result.failure(e))
        }
    }
}