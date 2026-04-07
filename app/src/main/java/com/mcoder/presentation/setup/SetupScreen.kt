package com.mcoder.presentation.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mcoder.util.setup.StepState

/**
 * First-run setup screen.
 */
@Composable
fun SetupScreen(
    onDone: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()
    val log by viewModel.log.collectAsState()
    val done by viewModel.done.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val steps by viewModel.steps.collectAsState()

    LaunchedEffect(Unit) {
        if (!done) viewModel.startSetup()
    }

    LaunchedEffect(done) {
        if (done) onDone()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Первичная настройка", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = status)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        steps.forEach { step ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = step.label, modifier = Modifier.weight(1f))
                Text(text = step.state.name)
            }
            when (step.state) {
                StepState.Running -> {
                    if (step.progress != null) {
                        LinearProgressIndicator(
                            progress = step.progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                StepState.Done -> {
                    LinearProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                StepState.Error -> {
                    LinearProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                StepState.Pending -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = viewModel::startSetup, modifier = Modifier.weight(1f)) {
                Text("Начать настройку")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = viewModel::resetSetup, modifier = Modifier.weight(1f)) {
                Text("Сброс")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(log) { line ->
                Text(text = line)
            }
        }
    }
}
