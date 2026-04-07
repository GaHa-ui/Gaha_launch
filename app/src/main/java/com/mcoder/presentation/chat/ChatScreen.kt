package com.mcoder.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Chat tab with streaming-ready UI.
 */
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val input by viewModel.input.collectAsState()
    val isBusy by viewModel.isAgentBusy.collectAsState()
    val status by viewModel.status.collectAsState()

    Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Чат", style = MaterialTheme.typography.titleLarge)
            Button(onClick = viewModel::clearHistory) {
                Text("Очистить историю")
            }
        }
        if (isBusy) {
            Spacer(modifier = Modifier.height(4.dp))
            val label = if (status.isBlank()) "Агент работает..." else status
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = message.role.uppercase())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = message.content)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = input,
            onValueChange = viewModel::onInputChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите сообщение...") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = viewModel::sendMessage, modifier = Modifier.fillMaxWidth()) {
            Text("Отправить")
        }
    }
}
