package com.mcoder.presentation.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.util.Constants
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
 * ViewModel for the embedded terminal.
 */
@HiltViewModel
class TerminalViewModel @Inject constructor() : ViewModel() {

    private val toolchainDir = File("${Constants.WORKSPACE_ROOT}/toolchain")
    private val toolchainBin = File(toolchainDir, "bin")
    private val executor = TerminalExecutor(toolchainDir)
    private val installer = ToolchainInstaller(toolchainDir)

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _cwd = MutableStateFlow(Constants.WORKSPACE_ROOT)
    val cwd: StateFlow<String> = _cwd.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _toolchainReady = MutableStateFlow(checkToolchain())
    val toolchainReady: StateFlow<Boolean> = _toolchainReady.asStateFlow()

    init {
        if (!_toolchainReady.value) {
            installToolchain()
        }
    }

    fun onInputChange(value: String) {
        _input.value = value
    }

    fun clear() {
        _lines.value = emptyList()
    }

    fun refreshToolchain() {
        _toolchainReady.value = checkToolchain()
    }

    fun installToolchain() {
        viewModelScope.launch {
            _status.value = "Installing toolchain..."
            installer.install { msg ->
                _status.value = msg
            }
            _toolchainReady.value = checkToolchain()
            if (_toolchainReady.value) {
                appendLine("Toolchain installed")
            } else {
                appendLine("Toolchain install failed")
            }
            _status.value = ""
        }
    }

    fun runCommand() {
        val cmd = _input.value.trim()
        if (cmd.isEmpty()) return
        _input.value = ""

        if (cmd.startsWith("cd ")) {
            val target = cmd.removePrefix("cd ").trim()
            changeDir(target)
            return
        }
        if (cmd == "cd" || cmd == "cd ~") {
            _cwd.value = Constants.WORKSPACE_ROOT
            return
        }

        viewModelScope.launch {
            appendLine("$ ${cmd}")
            _status.value = "Running..."
            val exit = executor.run(cmd, File(_cwd.value)) { line ->
                appendLine(line)
            }
            appendLine("(exit $exit)")
            _status.value = ""
        }
    }

    private fun changeDir(target: String) {
        val base = File(Constants.WORKSPACE_ROOT).canonicalFile
        val dest = File(_cwd.value, target).canonicalFile
        if (!dest.exists() || !dest.isDirectory || !dest.path.startsWith(base.path)) {
            appendLine("cd: no such directory: $target")
            return
        }
        _cwd.value = dest.absolutePath
    }

    private fun appendLine(text: String) {
        _lines.value = (_lines.value + text).takeLast(500)
    }

    private fun checkToolchain(): Boolean {
        val node = File(toolchainBin, "node")
        val busybox = File(toolchainBin, "busybox")
        return node.exists() && busybox.exists()
    }
}
