package com.mcoder.domain.model

/**
 * User preferences stored in Room.
 */
data class UserSettings(
    val id: Long = 1,
    val theme: String = "dark",
    val language: String = "ru",
    val editorFontSize: Int = 14,
    val defaultRemoteUrl: String = "http://localhost:4096",
    val ssePath: String = "/sse",
    val chatBackend: String = "cli",
    val cliCommandTemplate: String = "opencode chat --prompt {prompt}",
    val webViewJsEnabled: Boolean = true,
    val webViewLocalStorageEnabled: Boolean = true
)
