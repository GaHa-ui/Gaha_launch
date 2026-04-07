package com.mcoder.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.domain.model.UserSettings
import com.mcoder.domain.usecase.SettingsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes theme state from settings.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    settingsUseCases: SettingsUseCases
) : ViewModel() {
    val settings: StateFlow<UserSettings> = settingsUseCases.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())
}
