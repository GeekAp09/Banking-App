package com.mobile360.banking.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile360.banking.AppGraph

// CR-015: shown for 1-3 star ratings
@Composable
fun FeedbackScreen(
    stars: Int,
    onDone: () -> Unit,
    viewModel: FeedbackViewModel = viewModel { FeedbackViewModel(AppGraph.bank) },
) {
    val s by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onDone) { Text("Back") }
        Text("Send feedback")
        Text("Please do not include sensitive data such as account numbers.")
        if (s.submitted) {
            Text("Thanks for your feedback")
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
        } else {
            OutlinedTextField(
                value = s.comment,
                onValueChange = viewModel::setComment,
                label = { Text("Your feedback") },
                modifier = Modifier.fillMaxWidth(),
            )
            s.error?.let { Text(it) }
            Button(onClick = { viewModel.submit(stars) }, modifier = Modifier.fillMaxWidth()) {
                Text("Submit feedback")
            }
        }
    }
}
