package com.mcoder.util

/**
 * Simple blacklist for dangerous shell commands.
 */
object CommandPolicy {
    private val blocked = listOf("rm -rf", "sudo", "su", ":(){")

    fun isBlocked(command: String): Boolean {
        val normalized = command.lowercase()
        return blocked.any { token -> normalized.contains(token) }
    }
}
