package com.mobile360.banking.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.mobile360.banking.data.Times
import com.mobile360.banking.data.VaultDocEntity
import java.io.ByteArrayOutputStream
import java.io.File

/** Generates a PDF record of a vault document and saves it to Downloads via MediaStore. Returns the location or null. */
fun exportVaultDocToDownloads(context: Context, doc: VaultDocEntity): String? {
    val pdf = PdfDocument()
    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val title = Paint().apply { textSize = 22f; isFakeBoldText = true }
    val body = Paint().apply { textSize = 14f }
    page.canvas.drawText("Banking vault document", 48f, 70f, title)
    listOf(
        "Document: ${doc.name}", "Type: ${doc.badge}", "Size: ${doc.sizeLabel}",
        "Updated: ${Times.shortDate(doc.updatedAt)}", "Demo export of the vault record."
    ).forEachIndexed { i, line -> page.canvas.drawText(line, 48f, 110f + i * 26f, body) }
    pdf.finishPage(page)
    val out = ByteArrayOutputStream()
    pdf.writeTo(out)
    pdf.close()
    val fileName = doc.name.substringBeforeLast('.') + "-vault-record.pdf"
    return try {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { it.write(out.toByteArray()) } ?: return null
            "Downloads/$fileName"
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
            dir.mkdirs()
            File(dir, fileName).also { it.writeBytes(out.toByteArray()) }.absolutePath
        }
    } catch (e: Exception) {
        null
    }
}
