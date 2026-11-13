package com.mobile360.banking.ui.screens

import com.mobile360.banking.data.BankRepository
import kotlinx.coroutines.flow.first
import java.io.File
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobile360.banking.ui.components.BankCard

// CR-014: local JSON export of the user's records.
const val EXPORT_FILE_NAME = "banking-data-export.json"

private fun jsonValue(v: Any?): String = when (v) {
    null -> "null"
    is Number, is Boolean -> v.toString()
    else -> "\"" + v.toString().map {
        when (it) {
            '"' -> "\\\""
            '\\' -> "\\\\"
            '\n' -> "\\n"
            '\r' -> "\\r"
            '\t' -> "\\t"
            else -> if (it < ' ') "\\u%04x".format(it.code) else it.toString()
        }
    }.joinToString("") + "\""
}

/** Serialises named sections of flat records into a JSON document. */
fun exportToJson(sections: Map<String, List<Map<String, Any?>>>): String =
    sections.entries.joinToString(",\n", "{\n", "\n}") { (name, rows) ->
        "  ${jsonValue(name)}: [" + rows.joinToString(",") { row ->
            row.entries.joinToString(",", "{", "}") { "${jsonValue(it.key)}:${jsonValue(it.value)}" }
        } + "]"
    }

/** Reads one snapshot per section, reports progress (0-100) after each, and writes the file into [dir]. */
suspend fun writeDataExport(dir: File, bank: BankRepository, onProgress: (Int) -> Unit): File {
    val loaders: List<Pair<String, suspend () -> List<Map<String, Any?>>>> = listOf(
        "accounts" to { bank.accounts().first().map { mapOf("id" to it.id, "name" to it.name, "type" to it.type, "balanceCents" to it.balanceCents, "status" to it.status, "updatedAt" to it.updatedAt) } },
        "transactions" to { bank.transactions().first().map { mapOf("id" to it.id, "accountId" to it.accountId, "title" to it.title, "category" to it.category, "amountCents" to it.amountCents, "at" to it.at) } },
        "transfers" to { bank.transfers().first().map { mapOf("id" to it.id, "recipientName" to it.recipientName, "recipientPhone" to it.recipientPhone, "amountCents" to it.amountCents, "feeCents" to it.feeCents, "fromAccountId" to it.fromAccountId, "status" to it.status, "at" to it.at, "reference" to it.reference) } },
        "cards" to { bank.cards().first().map { mapOf("id" to it.id, "name" to it.name, "last4" to it.last4, "limitCents" to it.limitCents, "balanceCents" to it.balanceCents, "dueAt" to it.dueAt) } },
        "consents" to { bank.consents().first().map { mapOf("id" to it.id, "title" to it.title, "granted" to it.granted) } },
        "devices" to { bank.devices().first().map { mapOf("id" to it.id, "name" to it.name, "isCurrent" to it.isCurrent, "lastActiveAt" to it.lastActiveAt, "appInfo" to it.appInfo, "blocked" to it.blocked) } },
        "vaultDocuments" to { bank.vaultDocs("").first().map { mapOf("id" to it.id, "name" to it.name, "badge" to it.badge, "sizeLabel" to it.sizeLabel, "updatedAt" to it.updatedAt) } },
    )
    onProgress(0)
    val sections = LinkedHashMap<String, List<Map<String, Any?>>>()
    loaders.forEachIndexed { i, (name, load) ->
        sections[name] = load()
        onProgress((i + 1) * 90 / loaders.size)
    }
    dir.mkdirs()
    val file = File(dir, EXPORT_FILE_NAME)
    if (file.exists()) file.delete()
    file.writeText(exportToJson(sections))
    onProgress(100)
    return file
}

// CR-014: Export my data card; opens the system share sheet once when the file is ready.
@Composable
fun ExportDataCard(viewModel: PrivacyAndDevicesViewModel) {
    val export by viewModel.exportState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val running = export.progress != null
    LaunchedEffect(export.shareFile) {
        val file = export.shareFile ?: return@LaunchedEffect
        viewModel.exportShared()
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "My Banking data")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Share my data").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (e: Exception) {
            viewModel.exportFailed(e.message ?: "no app can share the file")
        }
    }
    BankCard {
        Text("Export my data", fontWeight = FontWeight.SemiBold)
        Text("Compiles your records into a JSON file you can share.")
        Spacer(Modifier.height(8.dp))
        export.progress?.let { p ->
            LinearProgressIndicator(progress = { p / 100f }, modifier = Modifier.fillMaxWidth())
            Text("Preparing your export… $p%")
            Spacer(Modifier.height(8.dp))
        }
        if (export.ready) Text("Export ready")
        export.error?.let { Text(it) }
        OutlinedButton(onClick = { viewModel.exportData(context.cacheDir) }, enabled = !running) {
            Text(if (export.error != null) "Retry" else "Export my data")
        }
    }
}
