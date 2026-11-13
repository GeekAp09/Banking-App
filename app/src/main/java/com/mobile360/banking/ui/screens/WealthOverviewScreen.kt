package com.mobile360.banking.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.Money
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.SkeletonList

@Composable
fun WealthOverviewScreen(
    onBack: () -> Unit,
    viewModel: WealthOverviewViewModel = viewModel(factory = vmFactory { WealthOverviewViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var consent by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var mask by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val barColors = listOf(AppColors.primaryContainer, AppColors.secondary, AppColors.secondaryContainer, Color(0xFF3E1269), Color(0xFF73777E))

    fun share() {
        val text = buildString {
            append("Banking wealth overview\nTotal wealth: ${Money.format(state.total)}\n")
            state.mix.forEach { append("${it.label}: ${it.percent}%\n") }
            state.holdings.forEach { append("${it.name} (${it.assetClass}): ${Money.formatWhole(it.valueCents)}\n") }
        }
        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
        context.startActivity(Intent.createChooser(send, "Share overview"))
    }

    Column(Modifier.fillMaxSize()) {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = AppColors.primary) }
                Text("Wealth overview", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                IconButton(onClick = { share() }) { Icon(Icons.Filled.Share, contentDescription = "Share overview", tint = AppColors.secondary) }
            }
        }
        if (!state.loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SkeletonList(rows = 4) }
            return@Column
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            WCard {
                Text("Total wealth", fontSize = 14.sp, color = Color(0xFF43474D))
                Text(Money.format(state.total), fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.padding(top = 8.dp))
                Text("Synced across ${Times2.institutions(state.linked.size + 1)}", fontSize = 12.sp, color = Color(0xFF43474D))
            }
            WCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Asset mix", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                    Text("${state.mix.size} Asset ${if (state.mix.size == 1) "Class" else "Classes"}", fontSize = 12.sp, color = AppColors.secondary)
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp).height(14.dp).background(Color(0xFFEAEEF4), CircleShape).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    state.mix.forEachIndexed { i, s ->
                        if (s.percent > 0) Box(Modifier.weight(s.percent.toFloat()).fillMaxHeight().background(barColors[i % barColors.size], CircleShape))
                    }
                }
                state.mix.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        pair.forEach { s ->
                            val idx = state.mix.indexOf(s)
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(12.dp).background(barColors[idx % barColors.size], CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(s.label, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("${s.percent}%", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF43474D))
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, Color(0x33C3C6CE))) {
                Column {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Holdings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                        Text("${state.holdings.size} ${if (state.holdings.size == 1) "Asset" else "Assets"}", fontSize = 12.sp, color = AppColors.secondary)
                    }
                    if (state.holdings.isEmpty()) Text("No holdings yet.", Modifier.padding(16.dp), fontSize = 14.sp, color = AppColors.secondary)
                    state.holdings.forEach { h ->
                        HorizontalDivider(color = Color(0x26C3C6CE))
                        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(AppColors.secondaryContainer.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(when (h.assetClass.lowercase()) { "bond", "bonds" -> Icons.Filled.AccountBalance; "share", "shares" -> Icons.Filled.ShowChart; else -> Icons.Filled.DonutLarge }, null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(h.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(h.assetClass, fontSize = 12.sp, color = Color(0xFF43474D))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Money.formatWhole(h.valueCents), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                                Text(String.format(java.util.Locale.US, "%.1f%% of total", h.valueCents * 100.0 / state.total.coerceAtLeast(1)), fontSize = 12.sp, color = AppColors.secondary)
                            }
                        }
                    }
                }
            }
            WCard {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = consent, onCheckedChange = { consent = it })
                    Text("I consent to share data from linked accounts", fontSize = 16.sp, modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = { dialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primaryContainer, contentColor = AppColors.onPrimary)
                ) { Icon(Icons.Filled.AddLink, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Link external account") }
                if (error != null) Text(error ?: "", color = Color(0xFFBA1A1A), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                if (message != null) Text(message ?: "", color = AppColors.primary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            WCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Linked accounts", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                    Text("${state.linked.size} connected", fontSize = 12.sp, color = Color(0xFF43474D))
                }
                if (state.linked.isEmpty()) Text("No linked accounts.", fontSize = 14.sp, color = AppColors.secondary, modifier = Modifier.padding(top = 8.dp))
                state.linked.forEach { l ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(Color(0xFFEAEEF4), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.AccountBalanceWallet, null, tint = AppColors.secondary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(l.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${l.mask} · ${if (l.verified) "Verified" else "Unverified"}", fontSize = 12.sp, color = Color(0xFF43474D))
                        }
                        TextButton(onClick = { viewModel.remove(l.id) }) { Text("Remove", color = Color(0xFFBA1A1A)) }
                    }
                }
            }
            OutlinedButton(onClick = { share() }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Filled.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Share overview")
            }
        }
    }
    if (dialog) {
        AlertDialog(
            onDismissRequest = { dialog = false },
            title = { Text("Link external account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Institution or account name") }, singleLine = true)
                    OutlinedTextField(mask, { v -> mask = v.filter { it.isDigit() }.take(4) }, label = { Text("Last 4 digits") }, singleLine = true)
                    Text(if (consent) "Consent given." else "Tick the consent box first; nothing is linked without it.", fontSize = 12.sp, color = AppColors.secondary)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.link(name, mask, consent)
                    dialog = false; if (consent && name.isNotBlank() && mask.length == 4) { name = ""; mask = "" }
                }) { Text("Link") }
            },
            dismissButton = { TextButton(onClick = { dialog = false }) { Text("Cancel") } }
        )
    }
}

private object Times2 {
    fun institutions(n: Int) = "$n ${if (n == 1) "institution" else "institutions"}"
}

@Composable
private fun WCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp, border = BorderStroke(1.dp, Color(0x33C3C6CE))) {
        Column(Modifier.padding(16.dp), content = content)
    }
}
