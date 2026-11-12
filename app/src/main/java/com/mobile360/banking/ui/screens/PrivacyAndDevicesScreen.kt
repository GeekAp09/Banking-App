package com.mobile360.banking.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.Times
import com.mobile360.banking.ui.components.Banner
import com.mobile360.banking.ui.components.BankCard
import com.mobile360.banking.ui.components.BankTopBar
import com.mobile360.banking.ui.components.RowDivider
import com.mobile360.banking.ui.components.StatusBadge
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory

@Composable
fun PrivacyAndDevicesScreen(
    onBack: () -> Unit,
    viewModel: PrivacyAndDevicesViewModel = viewModel(factory = vmFactory { PrivacyAndDevicesViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val closureError by viewModel.closureError.collectAsStateWithLifecycle()
    var confirmId by rememberSaveable { mutableStateOf<String?>(null) }
    var pickBlock by rememberSaveable { mutableStateOf(false) }
    var closureDialog by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }
    val blockable = state.devices.filter { !it.isCurrent && !it.blocked }

    Column(Modifier.fillMaxSize().background(AppColors.background)) {
        BankTopBar("Privacy and devices", onBack = onBack, actions = {
            Icon(Icons.Filled.Shield, contentDescription = "Security settings", tint = AppColors.secondary, modifier = Modifier.padding(end = 16.dp))
        })
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            state.loadError?.let { Banner(it, isError = true) }
            state.actionError?.let { err ->
                Banner(err, isError = true,
                    actionLabel = if (state.retryBlockId != null) "Retry" else null,
                    onAction = { state.retryBlockId?.let(viewModel::blockDevice) })
            }
            state.message?.let { Banner(it, actionLabel = "OK", onAction = viewModel::dismissMessage) }

            Row(
                Modifier.fillMaxWidth().background(AppColors.surfaceContainerLow, RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.VerifiedUser, null, tint = AppColors.secondary)
                Column {
                    Text("Client Data Protection", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.onSurface)
                    Text("Active monitoring prevents unapproved device access and strictly controls external data brokers.", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Devices, null, tint = AppColors.secondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Signed-in devices", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.primary, modifier = Modifier.weight(1f))
                    StatusBadge("${state.activeCount} Active", container = AppColors.secondaryContainer, content = AppColors.onSecondaryContainer)
                }
                BankCard {
                    state.devices.forEachIndexed { i, d ->
                        if (i > 0) RowDivider()
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(AppColors.secondaryContainer.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(if (d.isCurrent) Icons.Filled.Smartphone else Icons.Filled.PhoneAndroid, null, tint = AppColors.primary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(d.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.onSurface, modifier = Modifier.weight(1f, fill = false))
                                    if (d.isCurrent) StatusBadge("(this device)", container = AppColors.primaryContainer, content = AppColors.onPrimary)
                                    if (d.blocked) StatusBadge("Blocked", container = AppColors.errorContainer, content = AppColors.onErrorContainer)
                                }
                                val whenText = if (d.isCurrent) "Active now" else "Last active ${Times.ago(d.lastActiveAt)}"
                                Text("$whenText · ${d.appInfo}", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                            }
                            if (d.isCurrent) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "This device is active", tint = AppColors.secondary)
                            } else if (!d.blocked) {
                                OutlinedButton(
                                    onClick = { confirmId = d.id },
                                    modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Manage ${d.name}" },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, AppColors.outline.copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.primary)
                                ) { Text("Manage") }
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        if (blockable.size == 1) confirmId = blockable.first().id else pickBlock = true
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, AppColors.error.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.error)
                ) {
                    Icon(Icons.Filled.LockPerson, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Block device", fontWeight = FontWeight.SemiBold)
                }
                if (blockable.isEmpty()) {
                    Text("There are no other active devices to block.", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Sync, null, tint = AppColors.secondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Consents", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.primary)
                }
                BankCard {
                    state.consents.forEachIndexed { i, c ->
                        if (i > 0) RowDivider()
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(c.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.onSurface)
                                Text(c.description, style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                                if (!c.granted) Text("Withdrawn", style = MaterialTheme.typography.bodySmall, color = AppColors.error)
                            }
                            TextButton(
                                onClick = { viewModel.setConsent(c.id, !c.granted) },
                                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = (if (c.granted) "Withdraw " else "Grant ") + c.title }
                            ) { Text(if (c.granted) "Withdraw" else "Grant", fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }

            BankCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).background(AppColors.errorContainer.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.DeleteForever, null, tint = AppColors.error)
                    }
                    Column {
                        Text("Data Deletion & Account Closure", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.onSurface)
                        Text("Closing deletes data except records kept by law", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                    }
                }
                if (state.closurePending) {
                    Spacer(Modifier.size(12.dp))
                    Row(
                        Modifier.fillMaxWidth().background(AppColors.surfaceContainerLow, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.HourglassTop, null, tint = AppColors.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Request status: Pending", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = AppColors.onSurface, modifier = Modifier.weight(1f))
                        StatusBadge("In review", container = AppColors.secondaryContainer, content = AppColors.onSecondaryContainer)
                    }
                }
                Spacer(Modifier.size(12.dp))
                OutlinedButton(
                    onClick = { code = ""; viewModel.clearClosureError(); closureDialog = true },
                    enabled = !state.closurePending,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, AppColors.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = AppColors.errorContainer.copy(alpha = 0.3f), contentColor = AppColors.error)
                ) {
                    Icon(Icons.Filled.Gavel, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Request account closure", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Text("(Re-verify required)", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "Banking meets ISO/IEC 27001 and GDPR data sovereignty standards. Regulatory records retain compliance status.",
                style = MaterialTheme.typography.bodySmall, color = AppColors.outline, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }
    }

    val target = state.devices.firstOrNull { it.id == confirmId }
    if (target != null) {
        AlertDialog(
            onDismissRequest = { confirmId = null },
            title = { Text("Block ${target.name}?") },
            text = { Text("Its session ends now and it cannot sign in again.") },
            confirmButton = { TextButton(onClick = { viewModel.blockDevice(target.id); confirmId = null }) { Text("Block device", color = AppColors.error) } },
            dismissButton = { TextButton(onClick = { confirmId = null }) { Text("Cancel") } }
        )
    }
    if (pickBlock) {
        AlertDialog(
            onDismissRequest = { pickBlock = false },
            title = { Text("Block which device?") },
            text = {
                Column {
                    if (blockable.isEmpty()) Text("There are no other active devices to block.")
                    blockable.forEach { d ->
                        TextButton(onClick = { pickBlock = false; confirmId = d.id }, modifier = Modifier.heightIn(min = 48.dp)) { Text(d.name) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickBlock = false }) { Text("Close") } }
        )
    }
    if (closureDialog) {
        AlertDialog(
            onDismissRequest = { closureDialog = false },
            title = { Text("Re-verify to close account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your 6-digit code to confirm. This is a local demo: nothing is sent; the demo code is shown on the sign-in screen.")
                    Text("Retained by law: transaction records and audit logs are kept for the legally required period; all other personal data is deleted.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        label = { Text("Verification code") },
                        singleLine = true,
                        isError = closureError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    closureError?.let { Text(it, color = AppColors.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.requestClosure(code) { closureDialog = false } }) { Text("Submit request", color = AppColors.error) } },
            dismissButton = { TextButton(onClick = { closureDialog = false }) { Text("Cancel") } }
        )
    }
}
