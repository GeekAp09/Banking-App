package com.mobile360.banking.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.mobile360.banking.data.DemoAccounts
import java.io.ByteArrayOutputStream
import java.io.File
import com.mobile360.banking.ui.components.ValidatedTextField

/** Saves bytes to Downloads; returns a human-readable location or null on failure. */
fun saveToDownloads(context: Context, fileName: String, mime: String, bytes: ByteArray): String? {
    return try {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return null
            "Downloads/$fileName"
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
            dir.mkdirs()
            val f = File(dir, fileName)
            f.writeBytes(bytes)
            f.absolutePath
        }
    } catch (e: Exception) {
        null
    }
}

fun buildPdf(title: String, lines: List<String>): ByteArray {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val canvas = page.canvas
    val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true }
    val bodyPaint = Paint().apply { textSize = 14f }
    canvas.drawText(title, 48f, 70f, titlePaint)
    var y = 110f
    lines.forEach {
        canvas.drawText(it, 48f, y, bodyPaint)
        y += 26f
    }
    doc.finishPage(page)
    val out = ByteArrayOutputStream()
    doc.writeTo(out)
    doc.close()
    return out.toByteArray()
}

fun shareText(context: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Local demo re-verification: the demo code stands in for biometrics. Nothing is sent. */
@Composable
fun CodeVerifyDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onVerified: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                // CR-011: shared validated field with eye toggle and error border
                ValidatedTextField(
                    value = code,
                    onValueChange = { code = it.take(6).filter { c -> c.isDigit() }; wrong = false },
                    label = "Code (local demo: ${DemoAccounts.DEMO_CODE})",
                    error = if (wrong) "That code is not correct." else null,
                    secret = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (code == DemoAccounts.DEMO_CODE) onVerified() else wrong = true
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
