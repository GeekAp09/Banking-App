package com.mobile360.banking.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.vmFactory
import com.mobile360.banking.data.Money
import com.mobile360.banking.data.SupportInfo
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.theme.AppColors

import androidx.compose.runtime.saveable.rememberSaveable
@Composable
fun HomeScreen(
    onOpenWealthOverview: () -> Unit,
    onOpenAccountsAndCards: () -> Unit,
    onOpenPay: () -> Unit,
    onOpenCoach: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenMoreAndSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = vmFactory { HomeViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNotes by rememberSaveable { mutableStateOf(false) }
    val onSurface = Color(0xFF171C20)
    val variant = Color(0xFF43474D)

    Column(Modifier.fillMaxSize().background(Color(0xFFF6FAFF))) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).background(Color.White).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(40.dp).background(AppColors.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text(state.initials, color = AppColors.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Welcome back", fontSize = 12.sp, color = AppColors.secondary)
                Text("Home", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.primary)
            }
            IconButton(onClick = { showNotes = true }, modifier = Modifier.size(48.dp)) {
                BadgedBox(badge = {
                    val unread = state.notifications.count { !it.read }
                    if (unread > 0) Badge { Text(unread.toString()) }
                }) { Icon(Icons.Filled.Notifications, "Notifications", tint = AppColors.secondary) }
            }
            IconButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SupportInfo.PHONE))) },
                modifier = Modifier.size(48.dp)
            ) { Icon(Icons.Filled.SupportAgent, "Support Advisor", tint = AppColors.secondary) }
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().background(AppColors.primaryContainer, RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onOpenWealthOverview).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL WEALTH", fontSize = 12.sp, color = AppColors.primaryFixedDim, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Row(
                        Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape).padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.TrendingUp, null, tint = AppColors.primaryFixedDim, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("+4.2% this month", fontSize = 12.sp, color = AppColors.primaryFixedDim, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(Money.format(state.totalCents), fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = AppColors.onPrimaryContainer.copy(alpha = 0.2f))
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TouchApp, null, tint = AppColors.primaryFixedDim, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tap for overview", fontSize = 14.sp, color = AppColors.primaryFixedDim, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = AppColors.primaryFixedDim)
                }
            }
            HubRow(Icons.Filled.AccountBalanceWallet, AppColors.secondaryContainer, AppColors.primary, "Money: Accounts and Cards", state.accountsLine, onOpenAccountsAndCards)
            HubRow(Icons.Filled.QrCodeScanner, AppColors.secondaryContainer, AppColors.primary, "Pay: Scan or send", "QR pay, wire transfer, split bill", onOpenPay)
            HubRow(Icons.Filled.Psychology, Color(0xFFEFDBFF), Color(0xFF250048), "Coach: Insights and savings", state.coachLine, onOpenCoach)
            HubRow(Icons.Filled.Lock, Color(0xFFE4E9EE), AppColors.primary, "Vault: Documents", "Tax forms, statements, encrypted wills", onOpenVault)
            HubRow(Icons.Filled.ManageAccounts, Color(0xFFE4E9EE), AppColors.secondary, "More: Settings and help", "Security, 24/7 private banker, preferences", onOpenMoreAndSettings)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.VerifiedUser, null, tint = Color(0xFF73777E), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("FDIC Insured Up To \$2,500,000 via Sweep Network", fontSize = 12.sp, color = Color(0xFF73777E), fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showNotes) {
        AlertDialog(
            onDismissRequest = { showNotes = false },
            confirmButton = { TextButton(onClick = { showNotes = false }) { Text("Close") } },
            title = { Text("Notifications") },
            text = {
                if (state.notifications.isEmpty()) Text("No notifications.")
                else Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.notifications.forEach { n ->
                        Column {
                            Text(n.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(n.body, fontSize = 13.sp, color = variant)
                            Text(Times.ago(n.at), fontSize = 12.sp, color = AppColors.secondary)
                        }
                    }
                }
            }
        )
        // CR-013: What's New dialog
        val showWhatsNew by viewModel.showWhatsNew.collectAsStateWithLifecycle()
        val versionContext = LocalContext.current
        LaunchedEffect(Unit) {
            val info = versionContext.packageManager.getPackageInfo(versionContext.packageName, 0)
            viewModel.setCurrentVersion(androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(info))
        }
        if (showWhatsNew) AlertDialog(
            onDismissRequest = { viewModel.dismissWhatsNew() },
            confirmButton = { TextButton(onClick = { viewModel.dismissWhatsNew() }) { Text("Got it") } },
            title = { Text("What's New") },
            text = { Text("• Show-once release notes\n• Version and build shown in Settings\n• Performance and stability improvements") }
        )
    }
}

@Composable
private fun HubRow(icon: ImageVector, tileBg: Color, tileFg: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 68.dp).background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFC3C6CE).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).background(tileBg, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tileFg, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF171C20))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF43474D), maxLines = 1)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF73777E))
    }
}

