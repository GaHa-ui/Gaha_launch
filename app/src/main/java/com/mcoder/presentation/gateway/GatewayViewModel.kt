package com.mcoder.presentation.gateway

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.domain.usecase.GatewayUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gateway tab.
 */
@HiltViewModel
class GatewayViewModel @Inject constructor(
    private val gatewayUseCases: GatewayUseCases
) : ViewModel() {

    val agents = gatewayUseCases.observeAgents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mode = MutableStateFlow(GatewayMode.Chat)
    val mode: StateFlow<GatewayMode> = _mode.asStateFlow()

    fun setMode(mode: GatewayMode) {
        _mode.value = mode
    }

    fun setActiveAgent(agentId: String) {
        viewModelScope.launch { gatewayUseCases.setActiveAgent(agentId) }
    }
}
