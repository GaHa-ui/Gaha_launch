package com.mcoder.domain.usecase

import com.mcoder.domain.SettingsRepository

/**
 * Settings-related use cases.
 */
class SettingsUseCases(private val repository: SettingsRepository) {
    val observeSettings = repository::observeSettings
    val updateSettings = repository::updateSettings
}
