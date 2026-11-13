package com.mobile360.banking.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.PrefFlag
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.components.Banner
import com.mobile360.banking.ui.components.BankCard
import com.mobile360.banking.ui.components.BankListRow
import com.mobile360.banking.ui.components.OutlinedAction
import com.mobile360.banking.ui.components.RowDivider
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.SkeletonList

@Composable
fun MoreAndSettingsScreen(
    onOpenPrivacyAndDevices: () -> Unit,
    onOpenHelpAndSupport: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: MoreAndSettingsViewModel = viewModel(factory = vmFactory { MoreAndSettingsViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var notificationsOn by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val openSystemSettings: () -> Unit = {
        val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(i) } catch (_: Exception) {}
    }
    val versionText = remember {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "Banking v${info.versionName} (${PackageInfoCompat.getLongVersionCode(info)})"
        } catch (_: Exception) { "Banking" }
    }

    Column(Modifier.fillMaxSize().background(AppColors.surface)) {
        Surface(color = AppColors.surfaceContainerLowest, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(AppColors.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.VerifiedUser, null, tint = AppColors.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("More", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = AppColors.primary)
                    Text("Settings & Account Preferences", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                }
                IconButton(onClick = openSystemSettings, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = AppColors.secondary)
                }
            }
        }
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { SkeletonList(rows = 4) }
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            state.error?.let { Banner(it, isError = true) }
            Section("Accessibility") {
                BankCard {
                    SwitchRow(Icons.Filled.Contrast, "Easy Mode", "Larger text, high contrast", state.prefs.easyMode) {
                        viewModel.setFlag(PrefFlag.EASY_MODE, it)
                    }
                }
            }
            Section("Notification Settings") {
                BankCard {
                    if (!notificationsOn) {
                        Row(Modifier.fillMaxWidth().background(AppColors.secondaryContainer.copy(alpha = 0.35f)).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = AppColors.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Notifications are off. Enable in system settings.", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = AppColors.onSurface, modifier = Modifier.weight(1f))
                            Row(Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = openSystemSettings).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Settings", style = MaterialTheme.typography.labelMedium, color = AppColors.primary)
                                Icon(Icons.Filled.OpenInNew, null, tint = AppColors.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    SwitchRow(null, "Transaction alerts", "Real-time spending & deposit notifications", state.prefs.transactionAlerts) { viewModel.setFlag(PrefFlag.TRANSACTION_ALERTS, it) }
                    RowDivider()
                    SwitchRow(null, "Fraud alerts", "Suspicious activity and security warnings", state.prefs.fraudAlerts) { viewModel.setFlag(PrefFlag.FRAUD_ALERTS, it) }
                    RowDivider()
                    SwitchRow(null, "Saving round alerts", "Portfolio rebalancing and round-up confirmations", state.prefs.roundAlerts) { viewModel.setFlag(PrefFlag.ROUND_ALERTS, it) }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
                    Text("Privacy, devices, closure", style = MaterialTheme.typography.labelMedium, color = AppColors.secondary, modifier = Modifier.weight(1f))
                    Text("Manage permissions", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                }
                BankCard {
                    BankListRow("Devices", subtitle = Times.plural(state.activeDevices, "active device") + " connected", icon = Icons.Filled.Devices, onClick = onOpenPrivacyAndDevices)
                    RowDivider()
                    BankListRow("Consents", subtitle = "Data sharing & Open Banking", icon = Icons.Filled.VerifiedUser, onClick = onOpenPrivacyAndDevices)
                    RowDivider()
                    BankListRow("Close account", subtitle = "Account closure & data options", icon = Icons.Filled.NoAccounts, onClick = onOpenPrivacyAndDevices)
                }
            }
            Section("Help & Assistance") {
                BankCard {
                    BankListRow("Help and support", subtitle = "FAQs, 24/7 private concierge, security", icon = Icons.Filled.SupportAgent, onClick = onOpenHelpAndSupport)
                }
            }
            OutlinedAction("Sign out", onClick = { viewModel.signOut(onSignOut) }, icon = Icons.AutoMirrored.Filled.Logout, modifier = Modifier.fillMaxWidth())
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(versionText, style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant, textAlign = TextAlign.Center)
                Text("Regulated and protected under FSCS guarantee", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = AppColors.secondary, modifier = Modifier.padding(horizontal = 4.dp))
        content()
    }
}

@Composable
private fun SwitchRow(icon: androidx.compose.ui.graphics.vector.ImageVector?, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(value = checked, role = Role.Switch, onValueChange = onChange).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(Modifier.size(40.dp).background(AppColors.surfaceContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = AppColors.primary)
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = AppColors.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
