package com.mobile360.banking.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.data.SupportInfo
import com.mobile360.banking.ui.components.Banner
import com.mobile360.banking.ui.components.BankCard
import com.mobile360.banking.ui.components.BankTopBar
import com.mobile360.banking.ui.components.EmptyState
import com.mobile360.banking.ui.components.OutlinedAction
import com.mobile360.banking.ui.components.PrimaryButton
import com.mobile360.banking.ui.components.RowDivider
import com.mobile360.banking.ui.components.SearchField
import com.mobile360.banking.ui.components.StatusBadge
import com.mobile360.banking.ui.theme.AppColors
import com.mobile360.banking.vmFactory

@Composable
fun HelpAndSupportScreen(
    onBack: () -> Unit,
    viewModel: HelpAndSupportViewModel = viewModel(factory = vmFactory { HelpAndSupportViewModel(AppGraph.bank, AppGraph.session) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    var showInfo by rememberSaveable { mutableStateOf(false) }

    val voice = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            val text = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!text.isNullOrBlank()) viewModel.setQuery(text)
        }
    }
    val startVoice: () -> Unit = {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Search help")
        try { voice.launch(i) } catch (_: ActivityNotFoundException) { notice = "Voice search is not available on this device." }
    }
    val dial: () -> Unit = {
        try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SupportInfo.PHONE.filter { it.isDigit() }))) }
        catch (_: ActivityNotFoundException) { notice = "No phone app is available to place the call." }
    }

    Column(Modifier.fillMaxSize().background(AppColors.surface)) {
        BankTopBar("Help", onBack = onBack, actions = {
            IconButton(onClick = { showInfo = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Info, contentDescription = "Support information and guidelines", tint = AppColors.secondary)
            }
        })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                SearchField(query, viewModel::setQuery, "Search help (Type a question)", trailing = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setQuery("") }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                        IconButton(onClick = startVoice, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Mic, contentDescription = "Voice search", tint = AppColors.secondary)
                        }
                    }
                })
                Row(Modifier.padding(start = 12.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, null, tint = AppColors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Instant answers across encrypted security and account controls", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Frequently asked questions", style = MaterialTheme.typography.titleMedium, color = AppColors.primary, modifier = Modifier.weight(1f))
                StatusBadge("Updated today", container = AppColors.surfaceContainer, content = AppColors.secondary)
            }
            when {
                state.loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Banner(state.error!!, isError = true, actionLabel = "Retry", onAction = viewModel::retry)
                state.faqs.isEmpty() -> EmptyState("No results", "No help articles match \"$query\". Try different words or call us.")
                else -> BankCard {
                    state.faqs.forEachIndexed { index, faq ->
                        if (index > 0) RowDivider()
                        val open = expanded == faq.id
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                    .clickable(role = Role.Button) { expanded = if (open) null else faq.id }
                                    .semantics { stateDescription = if (open) "Expanded" else "Collapsed" }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.QuestionAnswer, null, tint = AppColors.secondary)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    StatusBadge(faq.group, container = AppColors.surfaceContainer, content = AppColors.secondary)
                                    Text(faq.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = AppColors.onSurface)
                                }
                                Icon(if (open) Icons.Filled.ExpandMore else Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = AppColors.outline)
                            }
                            if (open) Text(faq.answer, style = MaterialTheme.typography.bodyMedium, color = AppColors.onSurfaceVariant, modifier = Modifier.padding(start = 36.dp, bottom = 12.dp))
                        }
                    }
                }
            }
            BankCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).background(AppColors.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Security, null, tint = AppColors.onSecondaryContainer)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Security and compliance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.primary)
                        Text("Institutional custodian standards", style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.Verified, null, tint = AppColors.secondary)
                }
                Spacer(Modifier.size(12.dp))
                RowDivider(Modifier.padding(start = 0.dp))
                Spacer(Modifier.size(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Assurance(Icons.Filled.Key, "256-bit AES Encrypted", "Bank-grade data encryption at rest and in transit.")
                    Assurance(Icons.Filled.VerifiedUser, "PCI-DSS Level 1 Certified", "Strict annual third-party audits of payment pathways.")
                    Assurance(Icons.Filled.Fingerprint, "Local biometric & on-device key management", "Cryptographic tokens never leave your Secure Enclave.")
                }
                Spacer(Modifier.size(16.dp))
                Row(
                    Modifier.fillMaxWidth().background(AppColors.surfaceContainerLow, RoundedCornerShape(8.dp))
                        .border(1.dp, AppColors.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Gavel, null, tint = AppColors.secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Regulated by FINRA & SIPC account protection.", style = MaterialTheme.typography.bodySmall, color = AppColors.secondary)
                }
            }
        }
        Surface(color = AppColors.surfaceContainerLowest, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notice?.let { Banner(it, isError = true) }
                PrimaryButton(
                    "Chat with support",
                    onClick = { notice = "Live chat is not available in this demo. Call ${SupportInfo.PHONE} to reach support." },
                    icon = Icons.Filled.Chat,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedAction("Call ${SupportInfo.PHONE}", onClick = dial, icon = Icons.Filled.Call, modifier = Modifier.fillMaxWidth())
            }
        }
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Support information") },
            text = { Text("Search the FAQs above, or call ${SupportInfo.PHONE}. Banking staff never ask for your PIN or verification code.") },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun Assurance(icon: ImageVector, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = AppColors.secondary, modifier = Modifier.size(18.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.onSurface)
            Text(body, style = MaterialTheme.typography.bodySmall, color = AppColors.onSurfaceVariant)
        }
    }
}
