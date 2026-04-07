package com.mcoder.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Settings tab.
 */
@Composable
fun SettingsScreen(
    onOpenProot: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val tokenStored by viewModel.tokenStored.collectAsState()
    var profileName by remember { mutableStateOf("") }
    var profileUrl by remember { mutableStateOf("") }
    var profileToken by remember { mutableStateOf("") }
    var tokenInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Настройки", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = settings.defaultRemoteUrl,
            onValueChange = { viewModel.updateSettings(settings.copy(defaultRemoteUrl = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL удаленного сервера") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = settings.ssePath,
            onValueChange = { viewModel.updateSettings(settings.copy(ssePath = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("SSE путь (например /sse)") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = settings.chatBackend,
            onValueChange = { viewModel.updateSettings(settings.copy(chatBackend = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Backend чата (sse/cli)") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = settings.cliCommandTemplate,
            onValueChange = { viewModel.updateSettings(settings.copy(cliCommandTemplate = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("CLI команда ({prompt} вставляется)") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Пресеты CLI", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.applyCliPreset("opencode") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("OpenCode") }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.applyCliPreset("codex") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Codex") }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.applyCliPreset("openclaw") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("OpenClaw") }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.applyCliPreset("claude") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Claude Code") }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.applyCliPreset("gemini") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Gemini") }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.applyCliPreset("aider") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Aider") }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Размер шрифта редактора: ${settings.editorFontSize}")
        Slider(
            value = settings.editorFontSize.toFloat(),
            onValueChange = { viewModel.updateSettings(settings.copy(editorFontSize = it.toInt())) },
            valueRange = 10f..24f
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("JavaScript в WebView", modifier = Modifier.weight(1f))
            Switch(
                checked = settings.webViewJsEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(webViewJsEnabled = it)) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("LocalStorage в WebView", modifier = Modifier.weight(1f))
            Switch(
                checked = settings.webViewLocalStorageEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(webViewLocalStorageEnabled = it)) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = settings.language,
            onValueChange = { viewModel.updateSettings(settings.copy(language = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Язык (ru/en)") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = settings.theme,
            onValueChange = { viewModel.updateSettings(settings.copy(theme = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Тема (light/dark/system)") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = if (tokenStored.isBlank()) "API токен: не задан" else "API токен: сохранен")
        OutlinedTextField(
            value = tokenInput,
            onValueChange = { tokenInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API токен (SecureStore)") }
        )
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.Button(
            onClick = { viewModel.saveToken(tokenInput) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить токен")
        }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.OutlinedButton(
            onClick = { viewModel.clearToken(); tokenInput = "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Очистить токен")
        }
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.Button(
            onClick = onOpenProot,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Настроить Proot Distro")
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.Button(
            onClick = viewModel::exportSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Экспорт настроек (JSON)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.Button(
            onClick = viewModel::importSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Импорт настроек (JSON)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Профили серверов", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = profileName,
            onValueChange = { profileName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название профиля") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = profileUrl,
            onValueChange = { profileUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = profileToken,
            onValueChange = { profileToken = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Токен") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.Button(
            onClick = {
                viewModel.addProfile(profileName, profileUrl, profileToken)
                profileName = ""
                profileUrl = ""
                profileToken = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить профиль")
        }
        profiles.forEach { profile ->
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.deleteProfile(profile) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Удалить: ${profile.name} (${profile.url})")
            }
        }
    }
}
