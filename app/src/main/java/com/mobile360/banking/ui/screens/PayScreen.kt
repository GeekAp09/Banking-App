package com.mobile360.banking.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.EmptyState
import com.mobile360.banking.ui.components.InitialsAvatar
import com.mobile360.banking.ui.components.StatusBadge
import com.mobile360.banking.ui.theme.AppColors
import java.io.File
import com.mobile360.banking.ui.components.SkeletonList

private val Variant = Color(0xFF48525B)

@Composable
fun PayScreen(
    onBack: () -> Unit,
    onOpenConfirmAndReceipt: (id: String) -> Unit,
    viewModel: PayViewModel = viewModel(factory = vmFactory { PayViewModel(AppGraph.bank, AppGraph.session) })
) {
    val ctx = LocalContext.current
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val sync by viewModel.sync.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val me by viewModel.me.collectAsStateWithLifecycle()
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showMyQr by rememberSaveable { mutableStateOf(false) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) onOpenConfirmAndReceipt("qr")
        else viewModel.setMessage("No QR photo was captured. Try again or enter the recipient manually.")
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = AppColors.primary)
            }
            Text("Pay", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
        }
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, AppColors.brandBorder.copy(alpha = 0.3f))) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(AppColors.secondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PhotoCamera, null, tint = AppColors.primary)
                    }
                    Column {
                        Text("Camera needed to scan QR codes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                        Text(
                            "To quickly verify recipient account credentials and prevent misdirected transfers, enable camera access.",
                            fontSize = 12.sp, color = Variant, modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = { showRationale = true }, shape = CircleShape, color = AppColors.primary,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Row(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterVertically) {
                        Icon(Icons.Filled.QrCodeScanner, null, tint = AppColors.onPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Scan QR code", color = AppColors.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                }
                Surface(
                    onClick = { showMyQr = true }, shape = CircleShape, color = AppColors.secondaryContainer,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Row(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterVertically) {
                        Icon(Icons.Filled.QrCode2, null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Show my QR", color = AppColors.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }
                }
            }
            OutlinedTextField(
                value = query, onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Name or phone") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true, shape = RoundedCornerShape(8.dp),
                label = { Text("Search contacts") }
            )
            message?.let {
                Surface(shape = RoundedCornerShape(12.dp), color = AppColors.secondaryContainer) {
                    Text(it, Modifier.fillMaxWidth().padding(12.dp), fontSize = 13.sp, color = AppColors.primary)
                }
            }
            Column {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CONTACTS ON APP", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Variant)
                    val n = contacts?.size ?: 0
                    Text("$n available", fontSize = 12.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium)
                }
                val list = contacts
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, AppColors.brandBorder.copy(alpha = 0.3f))) {
                    when {
                        list == null -> Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) { SkeletonList(rows = 3) }
                        list.isEmpty() -> if (!sync) EmptyState("Contact sync is off", "Turn on Sync contacts to see friends who use Banking, or enter a recipient manually.")
                        else EmptyState("No contacts found", "No contact on Banking matches \"$query\".")
                        else -> Column {
                            list.forEachIndexed { i, c ->
                                if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.brandBorder.copy(alpha = 0.15f)))
                                Row(
                                    Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { onOpenConfirmAndReceipt(c.id) }.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InitialsAvatar(c.initials, size = 44)
                                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(c.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                                            if (c.instant) StatusBadge("Instant")
                                        }
                                        Text(c.phone, fontSize = 12.sp, color = Variant, maxLines = 1)
                                    }
                                    IconButton(onClick = { onOpenConfirmAndReceipt(c.id) }, modifier = Modifier.size(48.dp)) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Pay ${c.name}", tint = AppColors.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, AppColors.brandBorder.copy(alpha = 0.3f))) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Sync contacts", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("(Stop anytime)", fontSize = 12.sp, color = Variant)
                        }
                        Text("Continuously identify friends and verified peers on Banking.", fontSize = 12.sp, color = Variant)
                    }
                    Switch(checked = sync, onCheckedChange = viewModel::setSync, modifier = Modifier.semantics { contentDescription = "Sync contacts" })
                }
            }
            Surface(
                onClick = { onOpenConfirmAndReceipt("manual") }, shape = RoundedCornerShape(12.dp), color = Color.White,
                border = BorderStroke(1.dp, AppColors.brandBorder.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Keyboard, null, tint = AppColors.primary)
                    Text("Enter recipient manually", Modifier.weight(1f).padding(start = 12.dp), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                    Icon(Icons.Filled.ChevronRight, null, tint = AppColors.secondary)
                }
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            icon = { Icon(Icons.Filled.PhotoCamera, null) },
            title = { Text("Camera needed to scan QR codes") },
            text = { Text("Banking opens your camera app to take a photo of the QR code. Nothing is stored; you can enter the recipient manually instead.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    try {
                        val dir = File(ctx.cacheDir, "qr").apply { mkdirs() }
                        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", File(dir, "scan.jpg"))
                        camera.launch(uri)
                    } catch (e: Exception) {
                        viewModel.setMessage("The camera is not available on this device. Enter the recipient manually.")
                    }
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showRationale = false }) { Text("Not now") } }
        )
    }
    if (showMyQr) {
        AlertDialog(
            onDismissRequest = { showMyQr = false },
            title = { Text("Show my QR") },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.QrCode2, null, modifier = Modifier.size(120.dp), tint = AppColors.primary)
                    Text(me.name.ifBlank { "Banking customer" }, fontWeight = FontWeight.SemiBold)
                    Text(me.phone, color = Variant)
                    Text("Share these details so friends can pay you.", fontSize = 12.sp, color = Variant)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    shareText(ctx, "Pay me on Banking", "Pay ${me.name.ifBlank { "me" }} on Banking: ${me.phone}")
                }) { Text("Share") }
            },
            dismissButton = { TextButton(onClick = { showMyQr = false }) { Text("Close") } }
        )
    }
}
