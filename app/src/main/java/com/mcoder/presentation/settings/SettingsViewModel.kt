package com.mcoder.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.domain.model.ServerProfile
import com.mcoder.domain.model.UserSettings
import com.mcoder.domain.usecase.ServerProfileUseCases
import com.mcoder.domain.usecase.SettingsUseCases
import com.mcoder.util.Constants
import com.mcoder.util.NotificationHelper
import com.mcoder.util.SettingsJson
import com.mcoder.util.SecureStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCases: SettingsUseCases,
    private val serverProfileUseCases: ServerProfileUseCases,
    private val notifications: NotificationHelper,
    private val secureStore: SecureStore
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsUseCases.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val profiles: StateFlow<List<ServerProfile>> = serverProfileUseCases.observeProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _tokenStored = MutableStateFlow(secureStore.get("active_api_token") ?: "")
    val tokenStored: StateFlow<String> = _tokenStored.asStateFlow()

    fun updateSettings(settings: UserSettings) {
        viewModelScope.launch { settingsUseCases.updateSettings(settings) }
    }

    fun addProfile(name: String, url: String, token: String) {
        if (name.isBlank() || url.isBlank()) return
        viewModelScope.launch {
            serverProfileUseCases.addProfile(ServerProfile(id = 0, name = name, url = url, token = token))
        }
    }

    fun deleteProfile(profile: ServerProfile) {
        viewModelScope.launch { serverProfileUseCases.deleteProfile(profile) }
    }

    fun saveToken(token: String) {
        secureStore.put("active_api_token", token)
        _tokenStored.value = token
        notifications.notifyTaskFinished("Mcoder", "API токен сохранен")
    }

    fun clearToken() {
        secureStore.remove("active_api_token")
        _tokenStored.value = ""
        notifications.notifyTaskFinished("Mcoder", "API токен очищен")
    }

    fun applyCliPreset(preset: String) {
        val template = when (preset) {
            "opencode" -> "opencode chat --prompt {prompt}"
            "codex" -> "codex --prompt {prompt}"
            "openclaw" -> "openclaw chat --prompt {prompt}"
            "claude" -> "claude-code --prompt {prompt}"
            "gemini" -> "gemini --prompt {prompt}"
            "aider" -> "aider --message {prompt}"
            else -> return
        }
        updateSettings(settings.value.copy(cliCommandTemplate = template))
    }

    fun exportSettings() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File("${Constants.WORKSPACE_ROOT}/settings.json")
                file.parentFile?.mkdirs()
                file.writeText(SettingsJson.serialize(settings.value))
            }
            notifications.notifyTaskFinished("Mcoder", "Настройки экспортированы")
        }
    }

    fun importSettings() {
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                val file = File("${Constants.WORKSPACE_ROOT}/settings.json")
                if (!file.exists()) return@withContext null
                SettingsJson.deserialize(file.readText())
            }
            if (imported != null) {
                settingsUseCases.updateSettings(imported)
                notifications.notifyTaskFinished("Mcoder", "Настройки импортированы")
            }
        }
    }
}
