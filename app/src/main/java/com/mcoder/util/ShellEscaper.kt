package com.mcoder.util

/**
 * Escapes user input for safe single-quoted shell usage.
 */
object ShellEscaper {
    fun singleQuote(value: String): String {
        return "'" + value.replace("'", "'\"'\"'") + "'"
    }
}
