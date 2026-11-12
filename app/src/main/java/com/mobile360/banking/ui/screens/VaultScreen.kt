package com.mobile360.banking.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.vmFactory
import com.mobile360.banking.data.Times
import com.mobile360.banking.data.VaultDocEntity
import com.mobile360.banking.ui.components.EmptyState
import com.mobile360.banking.ui.components.OutlinedAction
import com.mobile360.banking.ui.components.PrimaryButton
import com.mobile360.banking.ui.theme.AppColors

import androidx.compose.material3.Badge
import androidx.compose.material.icons.filled.Badge
private val Variant = Color(0xFF43474D)
private val CardBorder = BorderStroke(1.dp, Color(0xFFC3C6CE).copy(alpha = 0.5f))

private fun docIcon(doc: VaultDocEntity): ImageVector = when {
    doc.name.endsWith(".jpg", true) || doc.name.endsWith(".png", true) -> Icons.Filled.Badge
    doc.name.contains("statement", true) || doc.name.contains("mortgage", true) -> Icons.Filled.AccountBalance
    else -> Icons.Filled.Description
}

private fun badgeIcon(badge: String): ImageVector = when {
    badge.contains("Identity", true) -> Icons.Filled.Verified
    badge.contains("Bank", true) -> Icons.Filled.Shield
    else -> Icons.Filled.Lock
}

private fun sizeText(bytes: Long): String =
    if (bytes >= 1_000_000) String.format(java.util.Locale.US, "%.1f MB", bytes / 1_000_000.0)
    else String.format(java.util.Locale.US, "%d KB", (bytes / 1000).coerceAtLeast(1))

@Composable
fun VaultScreen(
    onBack: () -> Unit,
    viewModel: VaultViewModel = viewModel(factory = vmFactory { VaultViewModel(AppGraph.bank, AppGraph.session) })
) {
    val ctx = LocalContext.current
    val docs by viewModel.docs.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.badgeFilter.collectAsStateWithLifecycle()
    val selected by viewModel.selectedId.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var filterMenu by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Pair<String, VaultDocEntity>?>(null) }
    var preview by remember { mutableStateOf<VaultDocEntity?>(null) }
    var upload by remember { mutableStateOf<Pair<String, String>?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            var name = "Document"
            var size = 0L
            ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val z = c.getColumnIndex(OpenableColumns.SIZE)
                    if (n >= 0) name = c.getString(n) ?: name
                    if (z >= 0 && !c.isNull(z)) size = c.getLong(z)
                }
            }
            upload = name to sizeText(size)
        }
    }

    fun perform(kind: String, doc: VaultDocEntity) {
        when (kind) {
            "open" -> { viewModel.logAccess(doc.id); preview = doc }
            "download" -> {
                viewModel.logAccess(doc.id)
                val lines = listOf(
                    "Document: ${doc.name}", "Type: ${doc.badge}", "Size: ${doc.sizeLabel}",
                    "Updated: ${Times.shortDate(doc.updatedAt)}", "Demo export of the vault record."
                )
                val file = doc.name.substringBeforeLast('.') + "-vault-record.pdf"
                val where = saveToDownloads(ctx, file, "application/pdf", buildPdf("Banking vault document", lines))
                viewModel.setMessage(if (where != null) "${doc.name} saved to $where" else "Could not save ${doc.name} on this device.")
            }
            "delete" -> viewModel.delete(doc)
        }
    }
    fun request(kind: String, doc: VaultDocEntity) {
        if (kind != "delete" && viewModel.isVerified()) perform(kind, doc) else pending = kind to doc
    }

    Column(Modifier.fillMaxSize()) {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = AppColors.primary)
                    }
                    Text("Vault", Modifier.weight(1f), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                    IconButton(onClick = { showSearch = !showSearch; if (!showSearch) viewModel.setQuery("") }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Search, contentDescription = "Search documents", tint = AppColors.secondary)
                    }
                    IconButton(onClick = viewModel::lock, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Lock, contentDescription = "Security Status: lock vault", tint = AppColors.secondary)
                    }
                }
                Row(Modifier.padding(start = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VerifiedUser, null, tint = AppColors.secondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("256-bit AES Encrypted Document Storage", fontSize = 12.sp, color = AppColors.secondary)
                }
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSearch) {
                OutlinedTextField(
                    query, viewModel::setQuery, Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Search documents") }, leadingIcon = { Icon(Icons.Filled.Search, null) }
                )
            }
            message?.let {
                Surface(shape = RoundedCornerShape(12.dp), color = AppColors.secondaryContainer) {
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(it, Modifier.weight(1f).padding(vertical = 12.dp), fontSize = 13.sp, color = AppColors.primary)
                        TextButton(onClick = { viewModel.setMessage(null) }) { Text("OK") }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = CardBorder, shadowElevation = 1.dp) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(AppColors.secondaryContainer), Alignment.Center) {
                        Icon(Icons.Filled.Fingerprint, null, tint = AppColors.primary)
                    }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Verify to open this document", Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.primary)
                            IconButton(onClick = { showInfo = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Info, contentDescription = "Security Notice Information", modifier = Modifier.size(16.dp), tint = Variant)
                            }
                        }
                        Text(
                            "Biometric ID or master PIN verification is strictly enforced before previewing or decrypting high-security assets.",
                            fontSize = 12.sp, color = Variant
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Documents", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = AppColors.primary)
                    Surface(shape = CircleShape, color = Color(0xFFDFE3E8), modifier = Modifier.padding(start = 8.dp)) {
                        Text((docs?.size ?: 0).toString(), Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, color = Variant)
                    }
                    Spacer(Modifier.weight(1f))
                    Box {
                        TextButton(onClick = { filterMenu = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Icon(Icons.Filled.Tune, null, tint = AppColors.secondary, modifier = Modifier.size(16.dp))
                            Text(" Filter", color = AppColors.secondary, fontSize = 14.sp)
                        }
                        DropdownMenu(expanded = filterMenu, onDismissRequest = { filterMenu = false }) {
                            DropdownMenuItem(text = { Text("All documents") }, onClick = { viewModel.setFilter(null); filterMenu = false })
                            badges.forEach { b ->
                                DropdownMenuItem(text = { Text(b) }, onClick = { viewModel.setFilter(b); filterMenu = false })
                            }
                        }
                    }
                }
                filter?.let {
                    Text("Showing: $it", fontSize = 12.sp, color = AppColors.secondary, modifier = Modifier.padding(horizontal = 4.dp))
                }
                val list = docs
                when {
                    list == null -> Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) { CircularProgressIndicator() }
                    list.isEmpty() -> EmptyState(
                        if (query.isNotBlank() || filter != null) "No documents found" else "Your vault is empty",
                        if (query.isNotBlank() || filter != null) "No document matches your search or filter." else "Upload a document to store it encrypted."
                    )
                    else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val sel = selected ?: list.first().id
                        list.forEach { doc ->
                            DocCard(
                                doc, selected = doc.id == sel,
                                onSelect = { viewModel.select(doc.id) },
                                onOpen = { request("open", doc) },
                                onDownload = { request("download", doc) },
                                onDelete = { request("delete", doc) }
                            )
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.CenterEnd) {
            Surface(onClick = { picker.launch(arrayOf("*/*")) }, shape = CircleShape, color = AppColors.primary, shadowElevation = 6.dp, modifier = Modifier.heightIn(min = 56.dp)) {
                Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.UploadFile, null, tint = AppColors.onPrimary)
                    Text("Upload document", color = AppColors.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    pending?.let { (kind, doc) ->
        CodeVerifyDialog(
            title = if (kind == "delete") "Delete ${doc.name}?" else "Verify to open",
            message = if (kind == "delete") "Enter your confirmation code to permanently delete this document." else "Re-verify to access ${doc.name}. Local demo: no code is sent.",
            confirmLabel = if (kind == "delete") "Delete" else "Verify",
            onDismiss = { pending = null },
            onVerified = { pending = null; viewModel.markVerified(); perform(kind, doc) }
        )
    }
    preview?.let { doc ->
        AlertDialog(
            onDismissRequest = { preview = null },
            icon = { Icon(docIcon(doc), null) },
            title = { Text(doc.name) },
            text = {
                Column {
                    Text(doc.badge, fontWeight = FontWeight.Medium)
                    Text("${doc.sizeLabel} · Updated ${Times.shortDate(doc.updatedAt)}", color = Variant)
                    Text("Decrypted preview (demo): this is where the document content would be shown. Access has been logged.", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            },
            confirmButton = { TextButton(onClick = { preview?.let { perform("download", it) }; preview = null }) { Text("Download") } },
            dismissButton = { TextButton(onClick = { preview = null }) { Text("Close") } }
        )
    }
    upload?.let { (name, size) ->
        AlertDialog(
            onDismissRequest = { upload = null },
            title = { Text("Store this document?") },
            text = { Text("$name ($size) will be stored encrypted in your vault.") },
            confirmButton = { TextButton(onClick = { viewModel.upload(name, size); upload = null }) { Text("Store") } },
            dismissButton = { TextButton(onClick = { upload = null }) { Text("Cancel") } }
        )
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Security notice") },
            text = { Text("Opening, downloading or deleting a document needs re-verification unless you verified in the last 5 minutes. Every access is logged.") },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun DocCard(
    doc: VaultDocEntity, selected: Boolean,
    onSelect: () -> Unit, onOpen: () -> Unit, onDownload: () -> Unit, onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp,
        border = if (selected) BorderStroke(2.dp, AppColors.primary.copy(alpha = 0.2f)) else CardBorder,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE4E9EE)), Alignment.Center) {
                    Icon(docIcon(doc), null, tint = AppColors.primary)
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(doc.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = if (selected) AppColors.primary.copy(alpha = 0.1f) else Color(0xFFEAEEF4)) {
                            Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(badgeIcon(doc.badge), null, modifier = Modifier.size(12.dp), tint = if (selected) AppColors.primary else Variant)
                                Text(" ${doc.badge}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (selected) AppColors.primary else Variant, maxLines = 1)
                            }
                        }
                    }
                    Text("${doc.sizeLabel} · Updated ${Times.shortDate(doc.updatedAt)}", fontSize = 12.sp, color = Variant, modifier = Modifier.padding(top = 2.dp))
                }
                if (!selected) {
                    IconButton(onClick = onOpen, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Visibility, contentDescription = "Open ${doc.name}", tint = AppColors.secondary)
                    }
                }
                Box {
                    IconButton(onClick = { menu = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More actions for ${doc.name}", tint = AppColors.secondary)
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Open") }, onClick = { menu = false; onOpen() })
                        DropdownMenuItem(text = { Text("Download") }, onClick = { menu = false; onDownload() })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
                    }
                }
            }
            if (selected) {
                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp).height(1.dp).background(Color(0xFF73777E).copy(alpha = 0.15f)))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton("Download", onDownload, Modifier.weight(1f), icon = Icons.Filled.Download)
                    OutlinedAction("Delete", onDelete, Modifier.weight(1f), icon = Icons.Filled.Delete)
                }
                Text(
                    "Confirmation code required prior to permanent deletion", fontSize = 11.sp, color = Color(0xFF73777E),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
