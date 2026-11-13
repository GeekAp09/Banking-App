package com.mobile360.banking.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mobile360.banking.data.formatBytes
import com.mobile360.banking.ui.components.BankCard

// CR-009: Storage & Cache section with stacked bar, legend, total and Clear Cache
@Composable
fun StorageCacheSection(viewModel: MoreAndSettingsViewModel) {
    val context = LocalContext.current
    val ui by viewModel.storage.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshStorage(context) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Storage & Cache", style = MaterialTheme.typography.titleMedium)
        BankCard {
            val info = ui.info
            if (info != null) {
                val (dbPct, cachePct, otherPct) = info.percentages()
                val rows = listOf(
                    Triple("Database", info.database, MaterialTheme.colorScheme.primary),
                    Triple("Cache", info.cache, MaterialTheme.colorScheme.tertiary),
                    Triple("Exports", info.other, MaterialTheme.colorScheme.outline),
                )
                val weights = listOf(dbPct, cachePct, otherPct)
                Row(
                    Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    rows.forEachIndexed { i, r ->
                        if (weights[i] > 0) {
                            Box(Modifier.weight(weights[i].toFloat()).height(12.dp).background(r.third))
                        }
                    }
                }
                rows.forEach { (label, bytes, color) ->
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Text(label, Modifier.weight(1f))
                        Text(formatBytes(bytes))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Total", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text(formatBytes(info.total), style = MaterialTheme.typography.titleSmall)
                }
            }
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OutlinedButton(
                onClick = { viewModel.clearCache(context) },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text("Clear Cache") }
            ui.message?.let { Text(it, Modifier.padding(top = 8.dp)) }
        }
    }
}
