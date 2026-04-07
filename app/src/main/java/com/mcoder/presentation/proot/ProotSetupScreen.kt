package com.mcoder.presentation.proot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Proot setup screen with Termux integration.
 */
@Composable
fun ProotSetupScreen(
    onBack: () -> Unit,
    viewModel: ProotViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Proot Distro", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Выбранный дистрибутив: ${status.distro}")
                Text(text = if (status.installed) "Статус: установлено" else "Статус: не установлено")
                if (status.sizeBytes > 0) {
                    Text(text = "Размер: ${status.sizeBytes} байт")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.selectDistro("ubuntu") }) { Text("Ubuntu") }
            OutlinedButton(onClick = { viewModel.selectDistro("debian") }) { Text("Debian") }
            OutlinedButton(onClick = { viewModel.selectDistro("archlinux") }) { Text("Arch") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = viewModel::installDistro, modifier = Modifier.fillMaxWidth()) {
            Text("Установить окружение")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = viewModel::loginDistro, modifier = Modifier.fillMaxWidth()) {
            Text("Войти в окружение")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = viewModel::installAgents, modifier = Modifier.fillMaxWidth()) {
            Text("Установить агентов")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = viewModel::removeDistro, modifier = Modifier.fillMaxWidth()) {
            Text("Удалить окружение")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = viewModel::openTermux) { Text("Открыть Termux") }
            OutlinedButton(onClick = onBack) { Text("Назад") }
        }
    }
}
