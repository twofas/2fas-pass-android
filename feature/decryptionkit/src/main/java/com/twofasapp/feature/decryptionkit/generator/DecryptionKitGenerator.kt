/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.decryptionkit.generator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.twofasapp.core.design.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DecryptionKitGenerator {
    private const val templateFilename = "vault-decryption-kit-template.pdf"

    private val wordPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.05f
    }

    fun generateFilename(): String {
        return "2FAS_Pass_DecryptionKit_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))}.pdf"
    }

    suspend fun generate(
        context: Context,
        fileUri: Uri,
        kit: DecryptionKit,
        includeMasterKey: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                generate(
                    context = context,
                    outputStream = outputStream,
                    kit = kit,
                    includeMasterKey = includeMasterKey,
                )
            }
        }
    }

    suspend fun generate(
        context: Context,
        outputStream: OutputStream,
        kit: DecryptionKit,
        includeMasterKey: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            context.assets.open(templateFilename).use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    val page = document.getPage(0)

                    val contentStream = PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true,
                    )

                    val fontStream = context.assets.open("fonts/Helvetica.ttf")
                    val font = PDType0Font.load(document, fontStream)

                    renderDate(
                        contentStream = contentStream,
                        font = font,
                    )

                    renderWords(
                        document = document,
                        contentStream = contentStream,
                        words = kit.words,
                    )

                    renderQrCode(
                        document = document,
                        context = context,
                        contentStream = contentStream,
                        font = font,
                        content = kit.generateQrCodeContent(includeMasterKey),
                    )

                    contentStream.close()

                    document.save(outputStream)

                    document.close()
                }
            }
        }
    }

    private fun renderDate(
        contentStream: PDPageContentStream,
        font: PDType0Font,
    ) {
        val x = 552f
        val y = 1020f

        val formattedDateTime = "Created on ${
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
        } at ${
            LocalDateTime.now().toLocalTime().format(
                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault()),
            )
        }"

        contentStream.beginText()
        contentStream.setFont(font, 12f)
        contentStream.newLineAtOffset(x, y)
        contentStream.showText(formattedDateTime)
        contentStream.endText()
    }

    private fun renderWords(
        document: PDDocument,
        contentStream: PDPageContentStream,
        words: List<String>,
    ) {
        val x = 100f
        val y = 636f
        val wordsLineHeight = 24f

        words.forEachIndexed { index, word ->
            val bitmap = renderWordAsBitmap(word)
            val image = LosslessFactory.createFromImage(document, bitmap)

            contentStream.drawImage(
                image,
                x,
                y - wordsLineHeight * index,
                image.width.toFloat(),
                image.height.toFloat(),
            )
        }
    }

    @SuppressLint("UseKtx")
    private fun renderQrCode(
        context: Context,
        document: PDDocument,
        contentStream: PDPageContentStream,
        font: PDType0Font,
        content: String,
    ) {
        val qrX = 415f
        val qrY = 330f
        val qrSize = 346
        val logoSize = 60

        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
        )

        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints)

        val qrBitmap = createBitmap(qrSize, qrSize).apply {
            for (x in 0 until qrSize) {
                for (y in 0 until qrSize) {
                    setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }

        val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.brand_logo_border)
        val logoScaled = logoBitmap.scale(logoSize, ((logoSize * logoBitmap.height) / logoBitmap.width.toFloat()).toInt())

        val qrImage = LosslessFactory.createFromImage(document, qrBitmap)
        val logoImage = LosslessFactory.createFromImage(document, logoScaled)

        contentStream.drawImage(qrImage, qrX, qrY, qrSize.toFloat(), qrSize.toFloat())

        contentStream.drawImage(
            logoImage,
            qrX + qrSize / 2f - logoScaled.width / 2f,
            qrY + qrSize / 2f - logoScaled.height / 2f,
            logoScaled.width.toFloat(),
            logoScaled.height.toFloat(),
        )

        contentStream.beginText()
        contentStream.setFont(font, 13f)
        contentStream.newLineAtOffset(qrX + 48, qrY - 20)
        contentStream.showText("Scan this QR code instead of retyping your")
        contentStream.newLineAtOffset(30f, -18f)
        contentStream.showText("Secret Key and Master Password.")
        contentStream.endText()
    }

    private fun renderWordAsBitmap(text: String): Bitmap {
        val textWidth = wordPaint.measureText(text)
        val fontMetrics = wordPaint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top

        val padding = 1
        val bitmapWidth = (textWidth + padding).toInt()
        val bitmapHeight = (textHeight + padding).toInt()

        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        val canvas = Canvas(bitmap)

        val x = 0f
        val y = -fontMetrics.top + padding / 2f

        val path = Path()
        wordPaint.getTextPath(text, 0, text.length, x, y, path)

        canvas.drawPath(path, wordPaint)

        return bitmap
    }
}