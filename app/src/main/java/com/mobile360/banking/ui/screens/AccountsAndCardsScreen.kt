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
import com.mobile360.banking.data.CardEntity
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.Times
import com.mobile360.banking.data.TransactionEntity
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.ScrollToTopLazyColumn

@Composable
fun AccountsAndCardsScreen(
    onOpenWealthOverview: () -> Unit,
    onOpenPay: () -> Unit = {},
    viewModel: AccountsAndCardsViewModel = viewModel(factory = vmFactory { AccountsAndCardsViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    var review by remember { mutableStateOf<CardEntity?>(null) }
    val tabs = listOf("Savings", "Cards")

    Column(Modifier.fillMaxSize()) {
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Column {
                Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Money", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profile and settings", tint = AppColors.secondary, modifier = Modifier.size(24.dp))
                }
                TabRow(selectedTabIndex = tab, containerColor = Color.White, contentColor = AppColors.primary) {
                    tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
                }
            }
        }
        if (!state.loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        val txs = if (tab == 0 && selected != null) state.transactions.filter { it.accountId == selected } else state.transactions
        val shown = if (showAll) txs else txs.take(3)
        ScrollToTopLazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (message != null) item {
                Surface(shape = RoundedCornerShape(12.dp), color = AppColors.secondaryContainer) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message ?: "", Modifier.weight(1f), fontSize = 13.sp, color = AppColors.onSecondaryFixed)
                        TextButton(onClick = { viewModel.show(null) }) { Text("Dismiss") }
                    }
                }
            }
            if (tab == 0) {
                if (state.accounts.isEmpty()) item { AcEmpty("No accounts", "Your accounts will appear here.") }
                items(state.accounts, key = { it.id }) { a ->
                    val sel = selected == a.id
                    AcCard(Modifier.clickable { selected = if (sel) null else a.id }, border = if (sel) AppColors.primary else null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AcIcon(Icons.Filled.AccountBalanceWallet, AppColors.secondaryContainer, AppColors.primary, 32)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(a.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF43474D))
                                Text("${a.status} · Updated ${Times.ago(a.updatedAt)}", fontSize = 12.sp, color = AppColors.secondary)
                            }
                            IconButton(onClick = onOpenPay) { Icon(Icons.Filled.SwapHoriz, contentDescription = "Quick Transfer", tint = AppColors.secondary) }
                        }
                        Text(Money.format(a.balanceCents), fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.padding(top = 8.dp))
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onOpenPay, colors = ButtonDefaults.buttonColors(containerColor = AppColors.secondaryContainer, contentColor = AppColors.primary)) {
                                Icon(Icons.Filled.ArrowUpward, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Transfer")
                            }
                            OutlinedButton(onClick = { viewModel.show("Deposits need a connection to your bank and are not available in this demo.") }) {
                                Icon(Icons.Filled.Add, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Deposit")
                            }
                        }
                    }
                }
            } else {
                if (state.cards.isEmpty()) item { AcEmpty("No cards", "Your cards will appear here.") }
                items(state.cards, key = { it.id }) { c ->
                    val days = ((c.dueAt - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
                    AcCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AcIcon(Icons.Filled.CreditCard, AppColors.primaryContainer, AppColors.onPrimaryContainer, 32)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${c.name} ending in ${c.last4}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.primary)
                                Text("Credit line · ${c.maskedNumber}", fontSize = 12.sp, color = AppColors.secondary)
                            }
                            Icon(Icons.Filled.Settings, contentDescription = "Card Settings", tint = AppColors.secondary)
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.Bottom) {
                            Text(Money.format(c.balanceCents), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                            Text("of ${Money.formatWhole(c.limitCents)} limit", fontSize = 12.sp, color = AppColors.secondary)
                        }
                        LinearProgressIndicator(
                            progress = { (c.balanceCents.toFloat() / c.limitCents.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(8.dp),
                            color = AppColors.primary, trackColor = Color(0xFFE4E9EE)
                        )
                        Row(Modifier.fillMaxWidth()) {
                            Text("Available: ${Money.format(c.availableCents)}", fontSize = 12.sp, color = AppColors.secondary, modifier = Modifier.weight(1f))
                            Text(if (days == 0L) "Due today" else "Due in ${Times.plural(days.toInt(), "day")}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.primary)
                        }
                    }
                }
                items(state.cards.filter { it.hasDiscrepancy }, key = { "d" + it.id }) { c ->
                    Surface(shape = RoundedCornerShape(12.dp), color = AppColors.errorContainer.copy(alpha = 0.4f), border = BorderStroke(1.dp, Color(0x33BA1A1A))) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Balance differs from bank record. Review.", Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFF171C20))
                            TextButton(onClick = { review = c }) {
                                Text("Review", color = Color(0xFFBA1A1A), fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            item {
                AcCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent transactions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showAll = !showAll }) { Text(if (showAll) "Show less" else "See all", color = AppColors.secondary) }
                    }
                    if (shown.isEmpty()) Text("No transactions yet.", fontSize = 14.sp, color = AppColors.secondary, modifier = Modifier.padding(vertical = 8.dp))
                    shown.forEachIndexed { i, t ->
                        if (i > 0) HorizontalDivider(color = Color(0x26C3C6CE))
                        AcTxRow(t)
                    }
                }
            }
            item {
                AcCard(Modifier.clickable(onClick = onOpenWealthOverview)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AcIcon(Icons.Filled.PieChart, AppColors.primaryContainer, AppColors.onPrimaryContainer, 40)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Wealth and portfolio overview", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
                            Text("Retirement, stocks & allocation targets", fontSize = 12.sp, color = AppColors.secondary)
                        }
                        Icon(Icons.Filled.ArrowForward, null, tint = AppColors.secondary)
                    }
                }
            }
        }
    }
    review?.let { c ->
        AlertDialog(
            onDismissRequest = { review = null },
            title = { Text("Balance differs from bank record") },
            text = {
                Column {
                    Text("${c.name} ending in ${c.last4}")
                    Text("Shown in app: ${Money.format(c.balanceCents)}")
                    Text("Bank record: ${Money.format(c.bankRecordCents)}")
                    Text("Difference: ${Money.format(kotlin.math.abs(c.balanceCents - c.bankRecordCents))}")
                }
            },
            confirmButton = { TextButton(onClick = { review = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun AcCard(modifier: Modifier = Modifier, border: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp,
        border = BorderStroke(1.dp, border ?: Color(0x33C3C6CE))) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun AcIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, bg: Color, fg: Color, size: Int) {
    Box(Modifier.size(size.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size((size / 2 + 2).dp))
    }
}

@Composable
private fun AcEmpty(title: String, message: String) {
    AcCard { Text(title, fontWeight = FontWeight.SemiBold, color = AppColors.primary); Text(message, fontSize = 13.sp, color = AppColors.secondary) }
}

@Composable
private fun AcTxRow(t: TransactionEntity) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AcIcon(if (t.amountCents >= 0) Icons.Filled.Payments else Icons.Filled.ShoppingBasket, if (t.amountCents >= 0) AppColors.secondaryContainer else Color(0xFFF0F4F9), AppColors.secondary, 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(t.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF171C20))
            Text("${t.category} • ${Times.dayLabel(t.at)}", fontSize = 12.sp, color = AppColors.secondary)
        }
        Text(Money.formatSigned(t.amountCents), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (t.amountCents >= 0) AppColors.primary else Color(0xFF171C20))
    }
}
