package com.mobile360.banking.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.ClientEntity
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.components.EmptyState
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory

import androidx.compose.runtime.saveable.rememberSaveable
@Composable
fun ClientListScreen(
    onOpenClientDetail: (id: String) -> Unit,
    onSignOut: () -> Unit,
    viewModel: ClientListViewModel = viewModel(factory = vmFactory { ClientListViewModel(AppGraph.bank, AppGraph.session) })
) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    var showRebalance by rememberSaveable { mutableStateOf(false) }
    val line = Color(0x4D73808C)
    val n = s.allClients.size

    Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFB))) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFD6E6F5)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AccountBalance, null, tint = AppColors.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Clients", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(AppColors.secondary))
                    Spacer(Modifier.width(6.dp))
                    Text("Wealth Manager Portal", fontSize = 12.sp, color = Color(0xFF48525B))
                }
            }
            IconButton(onClick = viewModel::toggleSort) {
                Icon(Icons.Filled.Tune, "Filter and sort clients", tint = AppColors.secondary)
            }
            IconButton(onClick = onSignOut) {
                Icon(Icons.AutoMirrored.Filled.Logout, "Sign out", tint = AppColors.secondary)
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, line)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total AUM Managed", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF48525B))
                            Text(
                                "$n active ${if (n == 1) "relationship" else "relationships"}", fontSize = 12.sp,
                                color = AppColors.primary, fontWeight = FontWeight.Medium,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0x99D6E6F5)).padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(Money.formatWhole(s.totalAumCents), fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("USD", fontSize = 14.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        if (s.totalAumCents > 0) {
                            Spacer(Modifier.height(12.dp))
                            val shades = listOf(AppColors.primary, AppColors.secondary, Color(0xFFAFCAE4))
                            Row(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Color(0xFFEAEEF4)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                s.allClients.sortedByDescending { it.aumCents }.forEachIndexed { i, c ->
                                    Box(Modifier.weight(c.aumCents.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(shades[i % 3]))
                                }
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = s.query, onValueChange = viewModel::onQuery, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50),
                    placeholder = { Text("Search clients") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = AppColors.secondary) },
                    trailingIcon = {
                        if (s.query.isNotEmpty()) IconButton(onClick = { viewModel.onQuery("") }) { Icon(Icons.Filled.Close, "Clear search") }
                        else Text("Name", fontSize = 12.sp, color = AppColors.secondary, modifier = Modifier.padding(end = 12.dp))
                    }
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Assigned clients", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1F23))
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(20.dp).clip(CircleShape).background(AppColors.secondaryContainer), contentAlignment = Alignment.Center) {
                            Text("${s.clients.size}", fontSize = 12.sp, color = Color(0xFF0F273D))
                        }
                    }
                    TextButton(onClick = viewModel::toggleSort) {
                        Text(if (s.sortDesc) "Sort by AUM" else "Sort by AUM (low first)", fontSize = 14.sp, color = AppColors.secondary)
                        Icon(if (s.sortDesc) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward, null, Modifier.size(16.dp), tint = AppColors.secondary)
                    }
                }
            }
            when {
                s.loading -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                s.error != null -> item {
                    Text(s.error!!, color = AppColors.onErrorContainer, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.errorContainer).padding(16.dp))
                }
                s.clients.isEmpty() -> item { EmptyState("No clients found", "No assigned client matches \"${s.query}\".") }
                else -> items(s.clients, key = { it.id }) { c -> ClientRow(c) { onOpenClientDetail(c.id) } }
            }
            if (!s.loading && s.error == null && n > 0) item {
                val over = s.overLimit.size
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F4F9)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFD6E6F5)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Cached, null, Modifier.size(18.dp), tint = AppColors.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Q3 Client Rebalance Due", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (over == 0) "All $n ${if (n == 1) "account" else "accounts"} aligned with risk limits"
                            else "$over of $n ${if (n == 1) "account" else "accounts"} above risk limits",
                            fontSize = 12.sp, color = Color(0xFF48525B)
                        )
                    }
                    TextButton(onClick = { showRebalance = true }) { Text("View", fontWeight = FontWeight.SemiBold, color = AppColors.primary) }
                }
            }
        }
    }

    if (showRebalance) {
        AlertDialog(
            onDismissRequest = { showRebalance = false },
            title = { Text("Q3 Client Rebalance Due") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    s.allClients.forEach { c ->
                        val ok = c.growthExposurePct <= c.riskThresholdPct
                        Text("${c.name}: growth exposure ${"%.1f".format(c.growthExposurePct)}% of ${"%.1f".format(c.riskThresholdPct)}% limit - ${if (ok) "aligned" else "above limit"}")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRebalance = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun ClientRow(c: ClientEntity, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White)
            .clickable(onClick = onClick).padding(12.dp)
            .semantics { contentDescription = "${c.name}, portfolio value ${Money.formatWhole(c.aumCents)}, ${c.strategy}" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(AppColors.primary), contentAlignment = Alignment.Center) {
            Text(c.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(c.name, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    c.strategy, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.onPrimaryContainer.copy(alpha = 1f).let { Color(0xFF0F273D) },
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFD6E6F5)).padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(c.tag, fontSize = 12.sp, color = Color(0xFF48525B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(Money.formatWhole(c.aumCents), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("${if (c.ytdPct >= 0) "+" else ""}${"%.1f".format(c.ytdPct)}% YTD", fontSize = 12.sp, color = AppColors.secondary, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = AppColors.secondary)
    }
}
