package com.mcoder.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.data.remote.SseAgentClient
import com.mcoder.data.termux.TermuxIntegration
import com.mcoder.domain.usecase.ChatUseCases
import com.mcoder.domain.usecase.SettingsUseCases
import com.mcoder.util.SecureStore
import com.mcoder.util.NotificationHelper
import com.mcoder.util.Constants
import com.mcoder.util.ShellEscaper
import com.mcoder.util.terminal.TerminalExecutor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the chat screen.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases,
    private val settingsUseCases: SettingsUseCases,
    private val sseClient: SseAgentClient,
    private val termux: TermuxIntegration,
    private val secureStore: SecureStore,
    private val notifications: NotificationHelper
) : ViewModel() {

    private val toolchainDir = File("${Constants.WORKSPACE_ROOT}/toolchain")
    private val toolchainBin = File(toolchainDir, "bin")
    private val embeddedExecutor = TerminalExecutor(toolchainDir)

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _isAgentBusy = MutableStateFlow(false)
    val isAgentBusy: StateFlow<Boolean> = _isAgentBusy.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val settingsState = settingsUseCases.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.mcoder.domain.model.UserSettings())

    val messages = chatUseCases.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onInputChange(value: String) {
        _input.value = value
    }

    fun sendMessage() {
        val content = _input.value.trim()
        if (content.isEmpty()) return
        _input.value = ""
        viewModelScope.launch {
            _isAgentBusy.value = true
            _status.value = "Отправка..."
            chatUseCases.sendMessage("user", content)
            val assistantId = chatUseCases.sendMessage("assistant", "")
            val createdAt = System.currentTimeMillis()
            val settings = settingsState.value
            val buffer = StringBuilder()
            val embeddedReady = File(toolchainBin, "node").exists() && File(toolchainBin, "git").exists()
            if (settings.chatBackend == "sse") {
                val url = buildSseUrl(settings.defaultRemoteUrl, settings.ssePath)
                val token = secureStore.get("active_api_token")
                try {
                    _status.value = "SSE поток..."
                    sseClient.stream(url, token, content).collectLatest { chunk ->
                        buffer.append(chunk)
                        chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
                    }
                } catch (_: Exception) {
                    buffer.append("(SSE недоступен, пробуем CLI)\\n")
                    chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
                    runCli(content, buffer, assistantId, createdAt, settings.cliCommandTemplate)
                }
            } else {
                if (embeddedReady) {
                    runEmbeddedCli(content, buffer, assistantId, createdAt, settings.cliCommandTemplate)
                } else if (termux.isTermuxInstalled()) {
                    runCli(content, buffer, assistantId, createdAt, settings.cliCommandTemplate)
                } else {
                    chatUseCases.updateMessage(assistantId, "assistant", "(Нет встроенного toolchain и Termux не установлен)", createdAt)
                }
            }
            if (buffer.isEmpty()) {
                chatUseCases.updateMessage(assistantId, "assistant", "(Пустой ответ)", createdAt)
            }
            notifications.notifyTaskFinished("Mcoder", "Ответ агента получен")
            _status.value = ""
            _isAgentBusy.value = false
        }
    }

    private fun buildSseUrl(base: String, path: String): String {
        val cleanBase = base.trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$cleanBase$cleanPath"
    }

    private suspend fun runCli(
        prompt: String,
        buffer: StringBuilder,
        assistantId: Long,
        createdAt: Long,
        template: String
    ) {
        val outFile = File("${Constants.WORKSPACE_ROOT}/cli/out.txt")
        outFile.parentFile?.mkdirs()
        val safePrompt = ShellEscaper.singleQuote(prompt)
        val command = buildCliCommand(template, safePrompt, outFile.absolutePath)
        _status.value = "CLI запуск..."
        val started = termux.runCommand(command, background = true)
        if (!started) {
            buffer.append("(Не удалось запустить CLI через Termux)\\n")
            chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
            return
        }
        pollOutput(outFile, buffer, assistantId, createdAt)
    }

    private suspend fun runEmbeddedCli(
        prompt: String,
        buffer: StringBuilder,
        assistantId: Long,
        createdAt: Long,
        template: String
    ) {
        val safePrompt = ShellEscaper.singleQuote(prompt)
        val command = template.replace("{prompt}", safePrompt)
        _status.value = "Embedded CLI..."
        val exit = embeddedExecutor.run(command, File(Constants.WORKSPACE_ROOT)) { line ->
            if (line.isNotBlank()) {
                buffer.append(line).append("\n")
                chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
            }
        }
        buffer.append("\n(exit $exit)")
        chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
    }

    private fun buildCliCommand(template: String, quotedPrompt: String, outPath: String): String {
        val cmd = template.replace("{prompt}", quotedPrompt)
        val script = "rm -f $outPath; $cmd > $outPath 2>&1; echo __MCODER_DONE__ >> $outPath"
        return "bash -lc ${ShellEscaper.singleQuote(script)}"
    }

    private suspend fun pollOutput(
        outFile: File,
        buffer: StringBuilder,
        assistantId: Long,
        createdAt: Long
    ) {
        var lastLen = 0
        var finished = false
        repeat(240) {
            if (outFile.exists()) {
                val text = outFile.readText()
                if (text.length > lastLen) {
                    lastLen = text.length
                    val cleaned = text.replace("__MCODER_DONE__", "")
                    buffer.clear()
                    buffer.append(cleaned)
                    chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
                }
                if (text.contains("__MCODER_DONE__")) {
                    finished = true
                    return
                }
            }
            delay(500)
        }
        if (!finished) {
            buffer.append("\\n(Таймаут CLI)")
            chatUseCases.updateMessage(assistantId, "assistant", buffer.toString(), createdAt)
        }
    }

    fun clearHistory() {
        viewModelScope.launch { chatUseCases.clearHistory() }
    }
}
