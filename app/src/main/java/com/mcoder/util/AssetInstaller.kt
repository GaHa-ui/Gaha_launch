package com.mcoder.util

import android.content.Context
import java.io.File

/**
 * Copies bundled assets into the workspace.
 */
class AssetInstaller(private val context: Context) {
    fun ensureInstallScript(): String {
        val targetDir = File("${Constants.WORKSPACE_ROOT}/scripts")
        if (!targetDir.exists()) targetDir.mkdirs()
        val target = File(targetDir, "install-agents.sh")
        if (!target.exists()) {
            context.assets.open("install-agents.sh").use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            target.setExecutable(true)
        }
        return target.absolutePath
    }
}
