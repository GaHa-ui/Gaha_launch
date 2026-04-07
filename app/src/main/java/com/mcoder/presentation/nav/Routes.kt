package com.mcoder.presentation.nav

/**
 * Navigation destinations used by the app.
 */
sealed class Routes(val route: String, val title: String) {
    data object Chat : Routes("chat", "Chat")
    data object Gateway : Routes("gateway", "Gateway")
    data object Files : Routes("files", "Files")
    data object Settings : Routes("settings", "Settings")
    data object Web : Routes("web", "Web")
    data object Proot : Routes("proot", "Proot")
    data object Terminal : Routes("terminal", "Terminal")
    data object Setup : Routes("setup", "Setup")
}
