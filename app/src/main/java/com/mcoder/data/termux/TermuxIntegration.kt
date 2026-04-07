package com.mcoder.data.termux

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.mcoder.util.CommandPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Best-effort integration with Termux and Termux:Tasker.
 */
@Singleton
class TermuxIntegration @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isTermuxInstalled(): Boolean = hasPackage("com.termux")

    fun isTaskerAddonInstalled(): Boolean = hasPackage("com.termux.tasker")

    fun openTermux(): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage("com.termux")
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    /**
     * Attempts to run a command via Termux:Tasker. Falls back to clipboard.
     */
    fun runCommand(command: String, background: Boolean = true): Boolean {
        if (CommandPolicy.isBlocked(command)) return false
        if (isTaskerAddonInstalled()) {
            val intent = Intent(ACTION_RUN_COMMAND).apply {
                setPackage("com.termux.tasker")
                putExtra(EXTRA_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/bash")
                putExtra(EXTRA_ARGUMENTS, arrayOf("-lc", command))
                putExtra(EXTRA_WORKDIR, "/data/data/com.termux/files/home")
                putExtra(EXTRA_BACKGROUND, background)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return try {
                context.startActivity(intent)
                true
            } catch (_: Exception) {
                copyToClipboard(command)
                false
            }
        }
        copyToClipboard(command)
        return false
    }

    private fun copyToClipboard(command: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("mcoder-command", command))
    }

    private fun hasPackage(pkg: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
            true
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val ACTION_RUN_COMMAND = "com.termux.tasker.ACTION_RUN_COMMAND"
        private const val EXTRA_COMMAND_PATH = "com.termux.tasker.EXTRA_COMMAND_PATH"
        private const val EXTRA_ARGUMENTS = "com.termux.tasker.EXTRA_ARGUMENTS"
        private const val EXTRA_WORKDIR = "com.termux.tasker.EXTRA_WORKDIR"
        private const val EXTRA_BACKGROUND = "com.termux.tasker.EXTRA_BACKGROUND"
    }
}
