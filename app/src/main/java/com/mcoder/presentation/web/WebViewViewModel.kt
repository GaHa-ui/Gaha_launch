package com.mcoder.presentation.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.data.remote.WebEndpointProbe
import com.mcoder.domain.usecase.SettingsUseCases
import com.mcoder.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for WebView screen with auto-detect support.
 */
@HiltViewModel
class WebViewViewModel @Inject constructor(
    settingsUseCases: SettingsUseCases,
    private val probe: WebEndpointProbe
) : ViewModel() {

    private val _manualUrl = MutableStateFlow("")
    val manualUrl: StateFlow<String> = _manualUrl.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _autoDetectedUrl = MutableStateFlow<String?>(null)

    val settings = settingsUseCases.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.mcoder.domain.model.UserSettings())

    val activeUrl = combine(_manualUrl, _autoDetectedUrl, settings) { manual, auto, settingsValue ->
        when {
            manual.isNotBlank() -> manual
            auto != null -> auto
            else -> settingsValue.defaultRemoteUrl
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), settings.value.defaultRemoteUrl)

    init {
        autoDetect()
    }

    fun updateManualUrl(value: String) {
        _manualUrl.value = value
    }

    fun updateToken(value: String) {
        _token.value = value
    }

    fun autoDetect() {
        viewModelScope.launch {
            val openClaw = probe.isReachable(Constants.OPENCLAW_URL)
            val openCode = probe.isReachable(Constants.OPENCODE_URL)
            _autoDetectedUrl.value = when {
                openClaw -> Constants.OPENCLAW_URL
                openCode -> Constants.OPENCODE_URL
                else -> null
            }
        }
    }
}
