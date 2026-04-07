package com.mcoder.presentation.terminal

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Embedded terminal screen with toolchain status.
 */
@Composable
fun TerminalScreen(viewModel: TerminalViewModel = hiltViewModel()) {
    val lines by viewModel.lines.collectAsState()
    val input by viewModel.input.collectAsState()
    val cwd by viewModel.cwd.collectAsState()
    val status by viewModel.status.collectAsState()
    val toolchainReady by viewModel.toolchainReady.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Терминал", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "CWD: $cwd")
        Text(text = if (toolchainReady) "Toolchain: OK (node + busybox)" else "Toolchain: не установлен")
        Text(text = "Toolchain path: /storage/emulated/0/Mcoder/toolchain/bin")
        if (status.isNotBlank()) {
            Text(text = status)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = viewModel::runCommand) { Text("Run") }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = viewModel::clear) { Text("Clear") }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = viewModel::refreshToolchain) { Text("Refresh") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = viewModel::installToolchain, enabled = !toolchainReady) {
                Text("Install toolchain")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = input,
            onValueChange = viewModel::onInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Команда") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(lines) { line ->
                Text(text = line)
            }
        }
    }
}
