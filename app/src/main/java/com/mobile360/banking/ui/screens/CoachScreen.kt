package com.mobile360.banking.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.SkeletonList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachScreen(
    viewModel: CoachViewModel = viewModel(factory = vmFactory { CoachViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val categories = listOf("Dining", "Groceries", "Transport", "Leisure")

    Column(Modifier.fillMaxSize()) {
        Surface(color = Color.White, border = BorderStroke(1.dp, Color(0x26C3C6CE))) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Psychology, null, tint = AppColors.primaryContainer, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Coach", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Tune, contentDescription = "Insights Settings", tint = AppColors.secondary)
            }
        }
        if (!state.loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SkeletonList(rows = 4) }
            return@Column
        }
        val recent = state.transactions.filter { it.amountCents < 0 }.take(5)
        val sel = recent.firstOrNull { it.id == selectedId } ?: recent.firstOrNull()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (message != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = AppColors.secondaryContainer) {
                    Row(Modifier.fillMaxWidth().padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message ?: "", Modifier.weight(1f), fontSize = 13.sp, color = AppColors.onSecondaryFixed)
                        TextButton(onClick = { viewModel.dismiss() }) { Text("Dismiss") }
                    }
                }
            }
            if (state.insights.isEmpty()) CCard { Text("No insights yet", fontWeight = FontWeight.SemiBold, color = AppColors.primary); Text("Insights appear as your transactions arrive.", fontSize = 13.sp, color = AppColors.secondary) }
            state.insights.forEach { ins ->
                CCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.background(AppColors.secondaryContainer, CircleShape).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, null, tint = Color(0xFF250048), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(ins.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.onSecondaryFixed)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Updated ${Times.ago(ins.updatedAt)}", fontSize = 12.sp, color = Color(0xFF43474D))
                    }
                    Text(ins.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.padding(top = 8.dp))
                    Text(ins.explanation, fontSize = 16.sp, color = Color(0xFF43474D), modifier = Modifier.padding(top = 4.dp))
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0x26C3C6CE))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Pace vs Target", fontSize = 12.sp, color = Color(0xFF43474D))
                            Text("${Money.format(ins.spentCents)} / ${Money.format(ins.targetCents)}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                        }
                        LinearProgressIndicator(
                            progress = { (ins.spentCents.toFloat() / ins.targetCents.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.width(128.dp).height(8.dp),
                            color = if (ins.spentCents > ins.targetCents) Color(0xFFBA1A1A) else AppColors.primary,
                            trackColor = Color(0xFFE4E9EE)
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.padding(horizontal = 4.dp)) {
                    Text("Recent transactions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                    Text("Review spend", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, Color(0x33C3C6CE))) {
                    Column {
                        if (recent.isEmpty()) Text("No transactions yet.", Modifier.padding(16.dp), fontSize = 14.sp, color = AppColors.secondary)
                        recent.forEachIndexed { i, t ->
                            if (i > 0) HorizontalDivider(color = Color(0x26C3C6CE))
                            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).background(Color(0xFFEAEEF4), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Receipt, null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(t.category, Modifier.background(Color(0xFFE4E9EE), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp, color = Color(0xFF43474D))
                                        Spacer(Modifier.width(6.dp))
                                        Text(Times.dayLabel(t.at), fontSize = 12.sp, color = Color(0xFF73777E))
                                    }
                                }
                                Text(Money.formatSigned(t.amountCents), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = { selectedId = t.id }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Change") }
                            }
                        }
                    }
                }
            }
            CCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Label, null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Change category", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                    Text("Quick categorize", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                }
                if (sel != null) Text("For ${sel.title}", fontSize = 12.sp, color = Color(0xFF43474D), modifier = Modifier.padding(top = 4.dp))
                FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { c ->
                        FilterChip(
                            selected = sel?.category == c,
                            onClick = { sel?.let { viewModel.setCategory(it.id, c) } },
                            label = { Text(c) },
                            leadingIcon = if (sel?.category == c) ({ Icon(Icons.Filled.Check, null, Modifier.size(18.dp)) }) else null,
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null, tint = Color(0xFF73777E), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("We will remember your change", fontSize = 12.sp, color = Color(0xFF43474D))
                }
            }
            CCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Round up to savings", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                        Text("Pause anytime", fontSize = 12.sp, color = Color(0xFF43474D))
                    }
                    Switch(checked = state.roundUps, onCheckedChange = { viewModel.toggleRoundUps(it) })
                }
                Row(Modifier.padding(top = 12.dp).fillMaxWidth().background(Color(0xFFF0F4F9), RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).background(AppColors.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Savings, null, tint = AppColors.primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Everyday Savings Vault", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.primary)
                        Text("Spare change transfers automatically after each purchase.", fontSize = 12.sp, color = Color(0xFF43474D))
                    }
                }
                if (sel != null) OutlinedButton(onClick = { viewModel.roundUp(sel.id) }, modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) { Text("Round up ${sel.title}") }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.padding(horizontal = 4.dp)) {
                    Text("Round history", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                    Text("This cycle", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.secondary)
                }
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, Color(0x33C3C6CE))) {
                    Column {
                        if (state.rounds.isEmpty()) Text("No rounds yet.", Modifier.padding(16.dp), fontSize = 14.sp, color = AppColors.secondary)
                        state.rounds.forEachIndexed { i, r ->
                            if (i > 0) HorizontalDivider(color = Color(0x26C3C6CE))
                            val saved = r.status.equals("SAVED", true)
                            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(32.dp).background(if (saved) AppColors.secondaryContainer else Color(0xFFEAEEF4), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(if (saved) Icons.Filled.CheckCircle else Icons.Filled.RemoveCircleOutline, null, tint = if (saved) AppColors.primary else Color(0xFF43474D), modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(r.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(r.note, fontSize = 12.sp, color = Color(0xFF43474D))
                                }
                                if (saved) Text(Money.formatSigned(r.amountCents), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    r.status.lowercase().replaceFirstChar { it.uppercase() },
                                    Modifier.then(if (saved) Modifier.background(AppColors.secondaryContainer, CircleShape) else Modifier.background(Color.Transparent, CircleShape)).padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 12.sp, color = if (saved) Color(0xFF001D36) else Color(0xFF43474D)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp, border = BorderStroke(1.dp, Color(0x33C3C6CE))) {
        Column(Modifier.padding(16.dp), content = content)
    }
}
