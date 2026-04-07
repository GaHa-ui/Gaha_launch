package com.mcoder.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.util.Constants
import com.mcoder.util.setup.SetupState
import com.mcoder.util.setup.SetupStep
import com.mcoder.util.setup.StepState
import com.mcoder.util.terminal.TerminalExecutor
import com.mcoder.util.terminal.ToolchainInstaller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * First-run setup workflow.
 */
@HiltViewModel
class SetupViewModel @Inject constructor() : ViewModel() {

    private val toolchainDir = File("${Constants.WORKSPACE_ROOT}/toolchain")
    private val toolchainBin = File(toolchainDir, "bin")
    private val installer = ToolchainInstaller(toolchainDir)
    private val executor = TerminalExecutor(toolchainDir)

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log.asStateFlow()

    private val _status = MutableStateFlow("Подготовка...")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _done = MutableStateFlow(SetupState.isDone())
    val done: StateFlow<Boolean> = _done.asStateFlow()

    private val _running = MutableStateFlow(false)

    private val npmPackages = listOf(
        "opencode" to "OpenCode",
        "@openai/codex" to "Codex",
        "openclaw" to "OpenClaw",
        "cursor-agent" to "Cursor CLI",
        "@google/gemini-cli" to "Gemini CLI"
    )

    private val _steps = MutableStateFlow(buildInitialSteps())
    val steps: StateFlow<List<SetupStep>> = _steps.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    fun startSetup() {
        viewModelScope.launch {
            if (_running.value) return@launch
            if (SetupState.isDone()) {
                _done.value = true
                return@launch
            }
            _running.value = true

            if (!ensureWorkspaceWritable()) {
                _status.value = "Нет доступа к памяти"
                updateStep("node", StepState.Error, null, "Storage permission")
                _running.value = false
                return@launch
            }

            updateStep("node", StepState.Running, 0f, "Downloading Node.js")
            _status.value = "Скачивание Node.js..."
            try {
                installer.install(
                    onProgress = { msg ->
                        append(msg)
                        _status.value = msg
                    },
                    onPercent = { p ->
                        updateStep("node", StepState.Running, p, "Node download")
                        updateOverallProgress()
                    }
                )
            } catch (_: Exception) {
                updateStep("node", StepState.Error, null, "Node download failed")
                _status.value = "Ошибка скачивания Node"
                _running.value = false
                return@launch
            }
            if (!File(toolchainBin, "node").exists()) {
                updateStep("node", StepState.Error, null, "Node failed")
                _status.value = "Ошибка установки Node"
                _running.value = false
                return@launch
            }
            updateStep("node", StepState.Done, 1f, "Node ready")

            _status.value = "Настройка npm..."
            append("npm config set prefix ${toolchainDir.absolutePath}")
            executor.run("npm config set prefix ${toolchainDir.absolutePath}", File(Constants.WORKSPACE_ROOT)) { line ->
                append(line)
            }

            updateStep("busybox", StepState.Running, 0f, "Downloading BusyBox")
            _status.value = "Установка BusyBox..."
            installer.installBusybox(
                onProgress = { msg ->
                    append(msg)
                    _status.value = msg
                },
                onPercent = { p ->
                    updateStep("busybox", StepState.Running, p, "BusyBox download")
                    updateOverallProgress()
                }
            )
            updateStep("busybox", StepState.Done, 1f, "BusyBox ready")

            updateStep("git", StepState.Running, 0f, "Downloading git")
            _status.value = "Установка git..."
            installer.installGit(
                onProgress = { msg ->
                    append(msg)
                    _status.value = msg
                },
                onPercent = { p ->
                    updateStep("git", StepState.Running, p, "Git download")
                    updateOverallProgress()
                }
            )
            updateStep("git", StepState.Done, 1f, "Git ready")

            _status.value = "Установка npm пакетов..."
            for ((pkg, label) in npmPackages) {
                val id = "npm:$pkg"
                updateStep(id, StepState.Running, null, "Installing $label")
                append("npm install -g $pkg")
                executor.run("npm install -g $pkg", File(Constants.WORKSPACE_ROOT)) { line ->
                    append(line)
                }
                updateStep(id, StepState.Done, 1f, "$label installed")
                updateOverallProgress()
            }

            _status.value = "Готово"
            SetupState.markDone()
            _done.value = true
            updateOverallProgress()
            _running.value = false
        }
    }

    fun resetSetup() {
        File("${Constants.WORKSPACE_ROOT}/.setup_done").delete()
        _done.value = false
        _steps.value = buildInitialSteps()
        _progress.value = 0f
    }

    private fun buildInitialSteps(): List<SetupStep> {
        val base = listOf(
            SetupStep(id = "node", label = "Node.js"),
            SetupStep(id = "busybox", label = "BusyBox"),
            SetupStep(id = "git", label = "Git")
        )
        val npm = npmPackages.map { (pkg, label) ->
            SetupStep(id = "npm:$pkg", label = "npm: $label")
        }
        return base + npm
    }

    private fun updateStep(id: String, state: StepState, progress: Float?, message: String) {
        _steps.value = _steps.value.map { step ->
            if (step.id == id) step.copy(state = state, progress = progress, message = message) else step
        }
    }

    private fun updateOverallProgress() {
        val steps = _steps.value
        if (steps.isEmpty()) {
            _progress.value = 0f
            return
        }
        val total = steps.size.toFloat()
        val sum = steps.sumOf { step ->
            when (step.state) {
                StepState.Done -> 1.0
                StepState.Running -> (step.progress ?: 0.5f).toDouble()
                StepState.Error -> 0.0
                StepState.Pending -> 0.0
            }
        }
        _progress.value = (sum / total).toFloat().coerceIn(0f, 1f)
    }

    private fun append(text: String) {
        _log.value = (_log.value + text).takeLast(200)
    }

    private fun ensureWorkspaceWritable(): Boolean {
        return try {
            val probe = File(Constants.WORKSPACE_ROOT, ".probe")
            probe.parentFile?.mkdirs()
            probe.writeText("ok")
            true
        } catch (_: Exception) {
            false
        }
    }
}
