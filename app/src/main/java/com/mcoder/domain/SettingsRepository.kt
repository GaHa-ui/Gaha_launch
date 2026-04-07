package com.mcoder.domain

import com.mcoder.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Settings data access contract.
 */
interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>
    suspend fun updateSettings(settings: UserSettings)
}
