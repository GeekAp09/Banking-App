package com.mobile360.banking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.Money
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardOptions
import com.mobile360.banking.ui.components.SkeletonList
@Composable
fun TradeScreen(
    clientId: String,
    onBack: () -> Unit,
    viewModel: TradeViewModel = viewModel(factory = vmFactory { TradeViewModel(AppGraph.bank, AppGraph.session) })
) {
    LaunchedEffect(clientId) { viewModel.load(clientId) }
    val s by viewModel.state.collectAsStateWithLifecycle()
    val c = s.client
    var showInstrument by remember { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }
    val p = s.preview
    val h = s.holding

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFB))) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate back") }
            Column(Modifier.weight(1f)) {
                Text("New trade", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                if (c != null) Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, fontSize = 12.sp, color = Color(0xFF48525B))
                    Box(Modifier.padding(horizontal = 6.dp).size(4.dp).clip(CircleShape).background(Color(0xFF73808C)))
                    Text(c.accountNo, fontSize = 11.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium)
                }
            }
            if (c != null) Row(
                Modifier.padding(end = 8.dp).clip(RoundedCornerShape(50)).background(Color(0xFFEAEEF4)).padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.VerifiedUser, null, Modifier.size(14.dp), tint = AppColors.secondary)
                Spacer(Modifier.width(4.dp))
                Text(c.riskProfile, fontSize = 12.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium)
            }
        }

        if (s.loading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { SkeletonList(rows = 4) }
        } else if (c == null || h == null) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("This client or instrument was not found.", fontSize = 16.sp)
                TextButton(onClick = onBack) { Text("Go back") }
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Action", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF48525B))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0xFFF0F4F9)).padding(4.dp)) {
                        listOf("BUY" to "Buy", "SELL" to "Sell").forEach { (k, label) ->
                            val sel = s.side == k
                            Row(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(50))
                                    .background(if (sel) AppColors.primaryContainer else Color.Transparent)
                                    .clickableRadio(sel) { viewModel.setSide(k) },
                                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (sel) { Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = Color.White); Spacer(Modifier.width(8.dp)) }
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (sel) Color.White else Color(0xFF48525B))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Instrument", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF48525B))
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF73808C))) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(h.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.width(8.dp))
                                        Text(h.symbol, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.secondary,
                                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEAEEF4)).padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                    Text("${Money.formatWhole(h.valueCents)} allocated • NAV ${Money.format(h.navCents)}", fontSize = 12.sp, color = Color(0xFF48525B), modifier = Modifier.padding(top = 4.dp))
                                }
                                IconButton(onClick = { showInstrument = true }) { Icon(Icons.Filled.ManageSearch, "Change instrument", tint = AppColors.secondary) }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0x1A73808C))
                            if (p != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.secondary))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Portfolio weight", fontSize = 12.sp, color = AppColors.secondary)
                                }
                                Text("${"%.1f".format(s.currentWeightPct)}% → ${"%.1f".format(p.postWeightPct)}% post-trade", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Quantity", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF48525B))
                        s.cashCents?.let { Text("Available cash: ${Money.format(it)}", fontSize = 12.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium) }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D73808C))) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(onClick = { viewModel.changeQty(-1) }, enabled = s.qty > 1, modifier = Modifier.size(48.dp)) { Icon(Icons.Filled.Remove, "Decrease units") }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${s.qty}", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    Text(if (s.qty == 1) "unit" else "units", fontSize = 12.sp, color = Color(0xFF48525B))
                                }
                                FilledTonalIconButton(onClick = { viewModel.changeQty(1) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Filled.Add, "Increase units") }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0x2673808C))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Text("Estimated execution", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF48525B))
                                if (p != null) Column(horizontalAlignment = Alignment.End) {
                                    Text(Money.format(p.estimatedCents), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                                    Text("Excludes 0.15% fee (${Money.format(p.feeCents)})", fontSize = 12.sp, color = AppColors.secondary)
                                }
                            }
                        }
                    }
                }

                if (p != null && p.exceedsRisk && s.side == "BUY") {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F4F9)).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(Color(0x26486279)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Shield, null, Modifier.size(20.dp), tint = AppColors.secondary) }
                        Column {
                            Text("Exceeds ${c.riskProfile} risk profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Execution will raise total growth equity exposure to ${"%.1f".format(p.newExposurePct)}%, exceeding the client’s approved ${c.riskProfile} threshold (${"%.1f".format(p.thresholdPct)}%).",
                                fontSize = 12.sp, color = Color(0xFF48525B), modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                p?.blockedReason?.let { reason ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.errorContainer.copy(alpha = 0.6f)).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFB3261E)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Lock, null, Modifier.size(18.dp), tint = Color.White) }
                        Column {
                            Text(reason, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB3261E))
                            Text("Advisory mandate ${c.mandate} requires re-verification before order execution. Request mandate consent via Secure Portal.", fontSize = 12.sp, color = Color(0xFF48525B), modifier = Modifier.padding(top = 2.dp))
                            TextButton(onClick = viewModel::requestAuthorization) {
                                Text("Send authorization prompt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB3261E))
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(14.dp), tint = Color(0xFFB3261E))
                            }
                            s.authNote?.let { Text(it, fontSize = 12.sp, color = Color(0xFF48525B)) }
                        }
                    }
                }

                s.result?.let {
                    Text(it, color = if (s.resultIsError) AppColors.onErrorContainer else Color(0xFF001E31),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (s.resultIsError) AppColors.errorContainer else AppColors.secondaryContainer).padding(14.dp))
                }
            }

            Column(Modifier.fillMaxWidth().background(Color.White).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 0.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Order type: Market", fontSize = 12.sp, color = Color(0xFF48525B))
                    if (s.blocked) Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Block, null, Modifier.size(14.dp), tint = Color(0xFFB3261E))
                        Spacer(Modifier.width(4.dp))
                        Text("Execution restricted", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFB3261E))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = viewModel::openConfirm, enabled = s.canSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryContainer, disabledContainerColor = Color(0xFFDFE3E8), disabledContentColor = Color(0xFF73808C))
                ) {
                    if (s.blocked) { Icon(Icons.Filled.Lock, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
                    Text("Confirm and verify", fontWeight = FontWeight.SemiBold)
                }
                Text("Banking Compliance Engine • Audit tracked", fontSize = 11.sp, color = Color(0xFF48525B), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
            }
        }
    }

    if (showInstrument && h != null) {
        AlertDialog(
            onDismissRequest = { showInstrument = false },
            title = { Text("Instrument") },
            text = { Text("Orders for this client are placed in ${h.name} (${h.symbol}), the client's primary mandate holding.") },
            confirmButton = { TextButton(onClick = { showInstrument = false }) { Text("OK") } }
        )
    }
    if (s.stage == TradeStage.Confirm && p != null && h != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismiss,
            title = { Text("Confirm trade") },
            text = { Text("${if (s.side == "BUY") "Buy" else "Sell"} ${s.qty} ${if (s.qty == 1) "unit" else "units"} of ${h.name} for ${c?.name} at about ${Money.format(p.estimatedCents)} plus ${Money.format(p.feeCents)} fee. A fresh verification is required next.") },
            confirmButton = { TextButton(onClick = { code = ""; viewModel.toVerify() }) { Text("Continue") } },
            dismissButton = { TextButton(onClick = viewModel::dismiss) { Text("Cancel") } }
        )
    }
    if (s.stage == TradeStage.Verify) {
        AlertDialog(
            onDismissRequest = viewModel::dismiss,
            title = { Text("Verify it's you") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your 6-digit code. Local demo: nothing is sent; the demo code is shown in the sign-in help.")
                    OutlinedTextField(
                        value = code, onValueChange = { code = it.filter(Char::isDigit).take(6) }, singleLine = true,
                        label = { Text("Verification code") }, isError = s.codeError != null,
                        supportingText = s.codeError?.let { e -> { Text(e) } },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.verifyAndSubmit(code) }, enabled = code.length == 6 && !s.busy) { Text("Verify and execute") } },
            dismissButton = { TextButton(onClick = viewModel::dismiss) { Text("Cancel") } }
        )
    }
}

private fun Modifier.clickableRadio(selected: Boolean, onClick: () -> Unit): Modifier =
    this.then(Modifier.selectable(selected = selected, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = onClick))
