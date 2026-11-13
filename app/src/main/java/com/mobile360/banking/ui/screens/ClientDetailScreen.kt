package com.mobile360.banking.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.components.PrimaryButton
import com.mobile360.banking.ui.components.TonalButton
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.SkeletonList

@Composable
fun ClientDetailScreen(
    clientId: String,
    onBack: () -> Unit,
    onOpenTrade: (id: String) -> Unit,
    viewModel: ClientDetailViewModel = viewModel(factory = vmFactory { ClientDetailViewModel(AppGraph.bank, AppGraph.session) })
) {
    LaunchedEffect(clientId) { viewModel.load(clientId) }
    val s by viewModel.state.collectAsStateWithLifecycle()
    val c = s.client
    val clipboard = LocalClipboardManager.current
    var menu by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val line = BorderStroke(1.dp, Color(0x4DC3C6CE))
    val cardColors = CardDefaults.cardColors(containerColor = Color.White)
    val shape = RoundedCornerShape(12.dp)

    Column(Modifier.fillMaxSize().background(Color(0xFFF6FAFF))) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to client list", tint = AppColors.primary) }
            Column(Modifier.weight(1f)) {
                Text(c?.name ?: "Client", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                if (c != null) Text("Acc #${c.accountNo}", fontSize = 12.sp, color = AppColors.secondary)
            }
            if (c != null) Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "More options", tint = AppColors.secondary) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Copy account number") }, onClick = {
                        clipboard.setText(AnnotatedString(c.accountNo)); copied = true; menu = false
                    })
                }
            }
        }

        when {
            s.error != null -> Message(s.error!!, onBack)
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SkeletonList(rows = 4) }
            c == null -> Message("This client was not found.", onBack)
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (copied) Text("Account number copied.", fontSize = 12.sp, color = AppColors.secondary)
                Card(colors = cardColors, shape = shape, border = line) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Portfolio Value", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                                Text(Money.formatWhole(c.aumCents), fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                            }
                            Row(
                                Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFF0F4F9)).padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.TrendingUp, null, Modifier.size(16.dp), tint = AppColors.primary)
                                Spacer(Modifier.width(4.dp))
                                Text("${if (c.ytdPct >= 0) "+" else ""}${"%.1f".format(c.ytdPct)}% YTD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0x33C3C6CE))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.HealthAndSafety, null, Modifier.size(18.dp), tint = AppColors.secondary)
                                Spacer(Modifier.width(6.dp))
                                Text(c.mandate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                            }
                            Text("${c.baseCurrency} Base", fontSize = 12.sp, color = AppColors.secondary)
                        }
                    }
                }

                Card(colors = cardColors, shape = shape, border = line) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Holdings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                            Text("Target vs Actual", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                        }
                        val shades = listOf(AppColors.primaryContainer, AppColors.secondary, AppColors.surfaceTint)
                        if (s.holdings.isNotEmpty()) {
                            Row(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(Color(0xFFEAEEF4)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                s.holdings.forEachIndexed { i, h -> Box(Modifier.weight(h.allocationPct.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(shades[i % 3])) }
                            }
                        }
                        Column {
                            s.holdings.forEachIndexed { i, h ->
                                if (i > 0) HorizontalDivider(color = Color(0x33C3C6CE))
                                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEAEEF4)), contentAlignment = Alignment.Center) {
                                        Icon(listOf(Icons.Filled.Public, Icons.Filled.AccountBalance, Icons.Filled.Payments)[i % 3], null, Modifier.size(20.dp), tint = shades[i % 3])
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(h.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(8.dp).clip(CircleShape).background(shades[i % 3]))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Allocation ${h.allocationPct}%", fontSize = 12.sp, color = AppColors.secondary)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(Money.formatWhole(h.valueCents), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                                        Text(h.subtitle, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                                    }
                                }
                            }
                        }
                    }
                }

                Card(colors = cardColors, shape = shape, border = line) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("Risk profile", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                            Text("Assessed ${Times.date(c.assessedAt)}", fontSize = 12.sp, color = AppColors.secondary)
                        }
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFEAEEF4)).padding(4.dp).selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ClientRiskProfiles.forEach { p ->
                                val sel = p.equals(s.risk, ignoreCase = true)
                                Row(
                                    Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) Color.White else Color.Transparent)
                                        .selectable(selected = sel, role = Role.RadioButton, onClick = { viewModel.selectRisk(p) }),
                                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (sel) Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = AppColors.primary)
                                    Text(p, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium, color = if (sel) AppColors.primary else AppColors.secondary, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Card(colors = cardColors, shape = shape, border = line) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = s.note, onValueChange = viewModel::editNote, modifier = Modifier.fillMaxWidth(),
                            label = { Text("Advice note") }, placeholder = { Text("Write advice") }, minLines = 3,
                            isError = s.noteError != null, supportingText = s.noteError?.let { e -> { Text(e) } }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, Modifier.size(14.dp), tint = AppColors.secondary)
                            Spacer(Modifier.width(4.dp))
                            Text("Logged to client audit trail for regulatory compliance", fontSize = 12.sp, color = AppColors.secondary)
                        }
                    }
                }

                s.saved?.let {
                    Text(it, color = Color(0xFF001E31), modifier = Modifier.fillMaxWidth().clip(shape).background(AppColors.secondaryContainer).padding(12.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
                    PrimaryButton("Save advice and profile", onClick = viewModel::save, modifier = Modifier.fillMaxWidth(), icon = Icons.Filled.CheckCircle)
                    TonalButton("New trade", onClick = { onOpenTrade(c.id) }, modifier = Modifier.fillMaxWidth(), icon = Icons.AutoMirrored.Filled.ArrowForward)
                }
            }
        }
    }
}

@Composable
private fun Message(text: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text, fontSize = 16.sp)
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text("Back to clients") }
    }
}
