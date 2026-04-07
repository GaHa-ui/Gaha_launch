package com.mcoder.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Web
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mcoder.presentation.chat.ChatScreen
import com.mcoder.presentation.files.FilesScreen
import com.mcoder.presentation.gateway.GatewayScreen
import com.mcoder.presentation.nav.Routes
import com.mcoder.presentation.proot.ProotSetupScreen
import com.mcoder.presentation.settings.SettingsScreen
import com.mcoder.presentation.setup.SetupScreen
import com.mcoder.presentation.terminal.TerminalScreen
import com.mcoder.presentation.web.WebScreen
import com.mcoder.util.setup.SetupState

/**
 * Root Compose tree with navigation and global UI elements.
 */
@Composable
fun McoderAppRoot() {
    val navController = rememberNavController()
    val items = listOf(
        Routes.Chat,
        Routes.Gateway,
        Routes.Files,
        Routes.Terminal,
        Routes.Settings
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = when (item) {
                                Routes.Chat -> Icons.Default.Chat
                                Routes.Gateway -> Icons.Default.Web
                                Routes.Files -> Icons.Default.Folder
                                Routes.Terminal -> Icons.Default.Code
                                Routes.Settings -> Icons.Default.Settings
                                Routes.Web -> Icons.Default.Web
                                Routes.Proot -> Icons.Default.Code
                                Routes.Setup -> Icons.Default.Add
                            }
                            Icon(imageVector = icon, contentDescription = item.title)
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showFabMenu = true }) {
                Icon(imageVector = Icons.Default.Web, contentDescription = "Quick Actions")
            }
        }
    ) { paddingValues ->
        val start = if (SetupState.isDone()) Routes.Chat.route else Routes.Setup.route
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Setup.route) {
                SetupScreen(onDone = {
                    navController.navigate(Routes.Chat.route) {
                        popUpTo(Routes.Setup.route) { inclusive = true }
                    }
                })
            }
            composable(Routes.Chat.route) { ChatScreen() }
            composable(Routes.Gateway.route) { GatewayScreen(onOpenWeb = { navController.navigate(Routes.Web.route) }) }
            composable(Routes.Files.route) { FilesScreen() }
            composable(Routes.Terminal.route) { TerminalScreen() }
            composable(Routes.Settings.route) { SettingsScreen(onOpenProot = { navController.navigate(Routes.Proot.route) }) }
            composable(Routes.Web.route) { WebScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.Proot.route) { ProotSetupScreen(onBack = { navController.popBackStack() }) }
        }
    }

    if (showFabMenu) {
        AlertDialog(
            onDismissRequest = { showFabMenu = false },
            confirmButton = {
                Button(onClick = {
                    showFabMenu = false
                    navController.navigate(Routes.Chat.route)
                }) { Text("Новый чат") }
            },
            dismissButton = {
                Button(onClick = {
                    showFabMenu = false
                    navController.navigate(Routes.Files.route)
                }) { Text("Новый файл") }
            },
            title = { Text("Быстрые действия") },
            text = {
                Button(onClick = {
                    showFabMenu = false
                    navController.navigate(Routes.Web.route)
                }) { Text("Открыть WebView") }
            }
        )
    }
}
