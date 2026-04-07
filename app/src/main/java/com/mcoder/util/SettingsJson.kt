package com.mcoder.util

import com.mcoder.domain.model.UserSettings
import org.json.JSONObject

/**
 * JSON serialization for settings.
 */
object SettingsJson {
    fun serialize(settings: UserSettings): String {
        return JSONObject()
            .put("theme", settings.theme)
            .put("language", settings.language)
            .put("editorFontSize", settings.editorFontSize)
            .put("defaultRemoteUrl", settings.defaultRemoteUrl)
            .put("ssePath", settings.ssePath)
            .put("chatBackend", settings.chatBackend)
            .put("cliCommandTemplate", settings.cliCommandTemplate)
            .put("webViewJsEnabled", settings.webViewJsEnabled)
            .put("webViewLocalStorageEnabled", settings.webViewLocalStorageEnabled)
            .toString(2)
    }

    fun deserialize(raw: String): UserSettings {
        val obj = JSONObject(raw)
        return UserSettings(
            theme = obj.optString("theme", "dark"),
            language = obj.optString("language", "ru"),
            editorFontSize = obj.optInt("editorFontSize", 14),
            defaultRemoteUrl = obj.optString("defaultRemoteUrl", "http://localhost:4096"),
            ssePath = obj.optString("ssePath", "/sse"),
            chatBackend = obj.optString("chatBackend", "cli"),
            cliCommandTemplate = obj.optString("cliCommandTemplate", "opencode chat --prompt {prompt}"),
            webViewJsEnabled = obj.optBoolean("webViewJsEnabled", true),
            webViewLocalStorageEnabled = obj.optBoolean("webViewLocalStorageEnabled", true)
        )
    }
}
