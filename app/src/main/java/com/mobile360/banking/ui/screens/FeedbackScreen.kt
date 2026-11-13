package com.mobile360.banking.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph
import com.mobile360.banking.vmFactory
import com.mobile360.banking.ui.components.BankCard
import com.mobile360.banking.ui.components.BankTopBar

// CR-002: feedback & bug report form with the list of saved reports
@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    viewModel: FeedbackViewModel = viewModel(factory = vmFactory { FeedbackViewModel(AppGraph.bank) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        BankTopBar(title = "Feedback & Bug Report", onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Feedback", "Bug").forEach { t ->
                    FilterChip(selected = state.type == t, onClick = { viewModel.setType(t) }, label = { Text(t) })
                }
            }
            OutlinedTextField(
                value = state.message,
                onValueChange = viewModel::setMessage,
                label = { Text("Message") },
                isError = state.error != null,
                modifier = Modifier.fillMaxWidth(),
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.saved?.let { Text(it) }
            Button(onClick = viewModel::submit, modifier = Modifier.fillMaxWidth()) { Text("Submit") }
            Text("My reports", style = MaterialTheme.typography.titleMedium)
            if (state.reports.isEmpty()) {
                Text("No reports yet.")
            } else {
                state.reports.forEach { r ->
                    BankCard {
                        Column(Modifier.padding(12.dp)) {
                            Text(r.type, style = MaterialTheme.typography.labelMedium)
                            Text(r.message)
                        }
                    }
                }
            }
        }
    }
}
