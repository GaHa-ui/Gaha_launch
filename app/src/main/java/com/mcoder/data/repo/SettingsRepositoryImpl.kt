package com.mcoder.data.repo

import com.mcoder.data.db.SettingsDao
import com.mcoder.data.db.UserSettingsEntity
import com.mcoder.domain.SettingsRepository
import com.mcoder.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Settings repository backed by Room.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {
    override fun observeSettings(): Flow<UserSettings> {
        return settingsDao.observeSettings().map { entity ->
            entity?.toDomain() ?: UserSettings()
        }
    }

    override suspend fun updateSettings(settings: UserSettings) {
        settingsDao.upsert(settings.toEntity())
    }

    private fun UserSettingsEntity.toDomain(): UserSettings {
        return UserSettings(
            id = id,
            theme = theme,
            language = language,
            editorFontSize = editorFontSize,
            defaultRemoteUrl = defaultRemoteUrl,
            ssePath = ssePath,
            chatBackend = chatBackend,
            cliCommandTemplate = cliCommandTemplate,
            webViewJsEnabled = webViewJsEnabled,
            webViewLocalStorageEnabled = webViewLocalStorageEnabled
        )
    }

    private fun UserSettings.toEntity(): UserSettingsEntity {
        return UserSettingsEntity(
            id = id,
            theme = theme,
            language = language,
            editorFontSize = editorFontSize,
            defaultRemoteUrl = defaultRemoteUrl,
            ssePath = ssePath,
            chatBackend = chatBackend,
            cliCommandTemplate = cliCommandTemplate,
            webViewJsEnabled = webViewJsEnabled,
            webViewLocalStorageEnabled = webViewLocalStorageEnabled
        )
    }
}
