package com.mcoder.presentation.gateway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Gateway screen showing agents and modes.
 */
@Composable
fun GatewayScreen(
    onOpenWeb: () -> Unit,
    viewModel: GatewayViewModel = hiltViewModel()
) {
    val agents by viewModel.agents.collectAsState()
    val mode by viewModel.mode.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Gateway", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val chatSelected = mode == GatewayMode.Chat
            val webSelected = mode == GatewayMode.Web
            if (chatSelected) {
                Button(onClick = { viewModel.setMode(GatewayMode.Chat) }) { Text("Чат") }
            } else {
                OutlinedButton(onClick = { viewModel.setMode(GatewayMode.Chat) }) { Text("Чат") }
            }
            if (webSelected) {
                Button(onClick = { viewModel.setMode(GatewayMode.Web) }) { Text("Веб-интерфейс") }
            } else {
                OutlinedButton(onClick = { viewModel.setMode(GatewayMode.Web) }) { Text("Веб-интерфейс") }
            }
        }
        if (mode == GatewayMode.Web) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenWeb) {
                Text("Открыть WebView")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(agents) { agent ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = agent.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = agent.description)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (agent.isInstalled) "Установлен" else "Не установлен")
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = { viewModel.setActiveAgent(agent.id) }) {
                                Text(if (agent.isActive) "Активен" else "Сделать активным")
                            }
                        }
                    }
                }
            }
        }
    }
}
