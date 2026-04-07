package com.mcoder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row settings entity.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val theme: String,
    val language: String,
    val editorFontSize: Int,
    val defaultRemoteUrl: String,
    val ssePath: String,
    val chatBackend: String,
    val cliCommandTemplate: String,
    val webViewJsEnabled: Boolean,
    val webViewLocalStorageEnabled: Boolean
)
