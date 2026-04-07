package com.mcoder.util.terminal

import com.mcoder.util.CommandPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Executes shell commands with a configurable toolchain PATH.
 */
class TerminalExecutor(private val toolchainDir: File) {

    suspend fun run(
        command: String,
        workingDir: File,
        onLine: suspend (String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        if (CommandPolicy.isBlocked(command)) {
            onLine("Blocked command")
            return@withContext 127
        }
        val process = ProcessBuilder(listOf("sh", "-lc", command))
            .directory(workingDir)
            .apply {
                val path = environment()["PATH"].orEmpty()
                val bin = File(toolchainDir, "bin").absolutePath
                val npmBin = File(toolchainDir, "lib/node_modules/.bin").absolutePath
                environment()["PATH"] = "$bin:$npmBin:$path"
            }
            .redirectErrorStream(true)
            .start()

        BufferedReader(InputStreamReader(process.inputStream)).useLines { lines ->
            lines.forEach { line -> onLine(line) }
        }

        process.waitFor()
    }
}
