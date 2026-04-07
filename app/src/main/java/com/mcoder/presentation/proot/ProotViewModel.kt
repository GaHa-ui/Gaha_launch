package com.mcoder.presentation.proot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mcoder.data.termux.TermuxIntegration
import com.mcoder.domain.model.ProotStatus
import com.mcoder.domain.usecase.ProotUseCases
import com.mcoder.util.NotificationHelper
import com.mcoder.util.AssetInstaller
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel coordinating Proot Distro actions via Termux.
 */
@HiltViewModel
class ProotViewModel @Inject constructor(
    private val prootUseCases: ProotUseCases,
    private val termux: TermuxIntegration,
    private val notifications: NotificationHelper,
    private val assets: AssetInstaller
) : ViewModel() {

    val status: StateFlow<ProotStatus> = prootUseCases.observeStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProotStatus())

    fun selectDistro(name: String) {
        val updated = status.value.copy(distro = name)
        viewModelScope.launch { prootUseCases.updateStatus(updated) }
    }

    fun installDistro() {
        val distro = status.value.distro
        val command = "pkg install proot-distro -y && proot-distro install $distro"
        termux.runCommand(command)
        notifications.notifyTaskFinished("Proot Distro", "Установка запущена: $distro")
        updateInstalled(true)
    }

    fun loginDistro() {
        val distro = status.value.distro
        val command = "proot-distro login $distro"
        termux.runCommand(command, background = false)
        notifications.notifyTaskFinished("Proot Distro", "Вход в окружение: $distro")
    }

    fun removeDistro() {
        val distro = status.value.distro
        val command = "proot-distro remove $distro"
        termux.runCommand(command)
        notifications.notifyTaskFinished("Proot Distro", "Удаление окружения: $distro")
        updateInstalled(false)
    }

    fun installAgents() {
        val distro = status.value.distro
        val scriptPath = assets.ensureInstallScript()
        val command = "proot-distro login $distro -- bash $scriptPath"
        termux.runCommand(command)
        notifications.notifyTaskFinished("Proot Distro", "Установка агентов запущена")
    }

    fun openTermux() {
        termux.openTermux()
    }

    private fun updateInstalled(installed: Boolean) {
        val updated = status.value.copy(
            installed = installed,
            lastUpdated = System.currentTimeMillis()
        )
        viewModelScope.launch { prootUseCases.updateStatus(updated) }
    }
}
