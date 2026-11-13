package com.mobile360.banking.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.vmFactory
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.SupportInfo
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.components.EmptyState
import com.mobile360.banking.ui.components.InitialsAvatar
import com.mobile360.banking.ui.components.OutlinedAction
import com.mobile360.banking.ui.components.PrimaryButton
import com.mobile360.banking.ui.components.TonalButton
import com.mobile360.banking.ui.theme.AppColors

import androidx.compose.foundation.layout.widthIn
private val Variant = Color(0xFF48525B)
private val CardBorder = BorderStroke(1.dp, AppColors.brandBorder.copy(alpha = 0.3f))

@Composable
fun ConfirmAndReceiptScreen(
    recipientId: String,
    onBack: () -> Unit,
    viewModel: ConfirmAndReceiptViewModel = viewModel(factory = vmFactory { ConfirmAndReceiptViewModel(AppGraph.bank, AppGraph.session) })
) {
    val ctx = LocalContext.current
    val s by viewModel.state.collectAsStateWithLifecycle()
    var showVerify by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()
    LaunchedEffect(recipientId) { viewModel.load(recipientId) }
    LaunchedEffect(s.error, s.receipt) { scroll.animateScrollTo(if (s.receipt != null) 0 else scroll.maxValue) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Confirm transfer", Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
            IconButton(onClick = { showHelp = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.HelpOutline, contentDescription = "Support or Help", tint = Variant)
            }
        }
        when {
            s.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            s.notFound -> Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyState("Recipient not found", "This contact is no longer available. Go back and pick another recipient.")
                PrimaryButton("Back to Pay", onBack)
            }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 16.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = CardBorder) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InitialsAvatar(s.initials, size = 48)
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text("Recipient", fontSize = 12.sp, color = Variant)
                                Text("To ${s.name.ifBlank { "new recipient" }}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Banking", fontSize = 14.sp, color = AppColors.secondary)
                                    Text("•", fontSize = 14.sp, color = AppColors.secondary)
                                    Text(s.masked, fontSize = 14.sp, color = AppColors.secondary, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEAEEF4)), Alignment.Center) {
                                Icon(Icons.Filled.VerifiedUser, null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (s.editable && s.receipt == null) {
                            OutlinedTextField(s.name, viewModel::setName, Modifier.fillMaxWidth(), label = { Text("Recipient name") }, singleLine = true, isError = s.editable && s.nameError != null, supportingText = { if (s.editable) s.nameError?.let { Text(it) } })
                            OutlinedTextField(
                                s.phone, viewModel::setPhone, Modifier.fillMaxWidth(), label = { Text("Recipient phone") }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), isError = s.editable && s.phoneError != null, supportingText = { if (s.editable) s.phoneError?.let { Text(it) } }
                            )
                        }
                    }
                }
                if (s.receipt == null) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = CardBorder) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Transfer Amount", fontSize = 14.sp, color = Variant, modifier = Modifier.padding(bottom = 4.dp))
                            OutlinedTextField(
                                value = s.amountText, onValueChange = viewModel::setAmount,
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                placeholder = { Text("0.00", fontSize = 32.sp) },
                                prefix = { Text("$", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppColors.primary) },
                                suffix = { Text("USD", fontSize = 14.sp, color = AppColors.secondary) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = s.amountError != null, supportingText = { s.amountError?.let { Text(it) } }
                            )
                            Surface(shape = CircleShape, color = Color(0xFFF0F4F9), modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = AppColors.secondary, modifier = Modifier.size(16.dp))
                                    Text("Available:", fontSize = 12.sp, color = Variant)
                                    Text(Money.format(s.availableCents), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("(${s.accountName})", fontSize = 12.sp, color = AppColors.secondary)
                                }
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                                listOf(20, 45, 100, 500).forEach { d ->
                                    val sel = s.amountCents == d * 100L
                                    Surface(
                                        onClick = { viewModel.setQuick(d) }, shape = RoundedCornerShape(8.dp),
                                        color = if (sel) AppColors.secondaryContainer else Color.White,
                                        border = BorderStroke(1.dp, if (sel) AppColors.primary.copy(alpha = 0.2f) else Variant.copy(alpha = 0.4f)),
                                        modifier = Modifier.heightIn(min = 40.dp).widthIn48()
                                    ) {
                                        Box(Modifier.padding(horizontal = 14.dp), Alignment.Center) {
                                            Text("$$d", fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (s.overPerTransfer) {
                        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.secondaryContainer.copy(alpha = 0.5f), border = BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.15f))) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.Info, null, tint = AppColors.primary)
                                Column {
                                    Text("Over the ${Money.formatWhole(com.mobile360.banking.data.Limits.PER_TRANSFER_CENTS)} per-transfer limit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                                    Text(
                                        "Institutional security compliance requires elevated dual-factor biometric authorization for cumulative daily transfers or large liquidity events.",
                                        fontSize = 12.sp, color = Variant, modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Column {
                        Surface(
                            onClick = { if (!s.sending && viewModel.validate()) showVerify = true },
                            shape = CircleShape, color = AppColors.primary, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) {
                            Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (s.sending) CircularProgressIndicator(Modifier.size(24.dp), color = AppColors.onPrimary, strokeWidth = 2.dp)
                                else Icon(Icons.Filled.Fingerprint, null, tint = AppColors.onPrimary)
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text("Verify and send", color = AppColors.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("(Biometric re-verify)", color = Color(0xFFACC9EE), fontSize = 12.sp)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = AppColors.onPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        s.error?.let {
                            Surface(shape = RoundedCornerShape(12.dp), color = AppColors.errorContainer, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(it, Modifier.padding(12.dp), color = AppColors.onErrorContainer, fontSize = 13.sp)
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.Center, Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, null, tint = AppColors.secondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("256-bit encrypted end-to-end clearing protocol", fontSize = 12.sp, color = Variant)
                        }
                    }
                }
                s.receipt?.let { r ->
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = CardBorder) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(32.dp).clip(CircleShape).background(AppColors.secondaryContainer), Alignment.Center) {
                                    Icon(Icons.Filled.Receipt, null, tint = AppColors.primary, modifier = Modifier.size(18.dp))
                                }
                                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                    Text("Receipt", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Reference record", fontSize = 12.sp, color = AppColors.secondary)
                                }
                                Surface(shape = CircleShape, color = Color(0xFFEAEEF4)) {
                                    Text(
                                        r.status.lowercase().replaceFirstChar { it.uppercase() }, Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            val fee = Money.format(r.feeCents) + if (s.instant) " (Instant)" else ""
                            val rows = listOf(
                                "Amount Delivered" to "${Money.format(r.amountCents)} sent",
                                "Timestamp" to Times.dateTime(r.at),
                                "Recipient Legal Name" to r.recipientName,
                                "Transfer Fee" to fee,
                                "Reference Identifier" to r.reference
                            )
                            rows.forEach { (k, v) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), Arrangement.SpaceBetween) {
                                    Text(k, fontSize = 12.sp, color = Variant)
                                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = if (k.startsWith("Reference")) FontFamily.Monospace else null)
                                }
                            }
                            note?.let {
                                Text(it, fontSize = 13.sp, color = AppColors.primary, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedAction("PDF Statement", {
                                    val lines = rows.map { "${it.first}: ${it.second}" }
                                    val where = saveToDownloads(ctx, "Banking-receipt-${r.reference}.pdf", "application/pdf", buildPdf("Banking transfer receipt", lines))
                                    note = if (where != null) "Saved to $where" else "Could not save the statement on this device."
                                }, Modifier.weight(1f), icon = Icons.Filled.Download)
                                TonalButton("Share Receipt", {
                                    shareText(ctx, "Banking receipt", rows.joinToString("\n") { "${it.first}: ${it.second}" })
                                }, Modifier.weight(1f), icon = Icons.Filled.Share)
                            }
                        }
                    }
                    PrimaryButton("Done", onBack, Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (showVerify) {
        CodeVerifyDialog(
            title = "Verify and send",
            message = "Re-verify to send ${Money.format(s.amountCents)} to ${s.name}. Local demo: nothing is sent to a real bank.",
            confirmLabel = "Verify and send",
            onDismiss = { showVerify = false },
            onVerified = { showVerify = false; viewModel.send() }
        )
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Need help?") },
            text = { Text("Call Banking support on ${SupportInfo.PHONE}.") },
            confirmButton = {
                TextButton(onClick = {
                    showHelp = false
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SupportInfo.PHONE)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) { Text("Call") }
            },
            dismissButton = { TextButton(onClick = { showHelp = false }) { Text("Close") } }
        )
    }
}

private fun Modifier.widthIn48(): Modifier = this.then(Modifier.widthIn(min = 48.dp))
