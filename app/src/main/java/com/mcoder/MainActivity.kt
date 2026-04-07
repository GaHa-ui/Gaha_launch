package com.mcoder

import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.net.Uri
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.mcoder.presentation.McoderAppRoot
import com.mcoder.presentation.theme.McoderTheme
import com.mcoder.presentation.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Main activity hosting the Compose UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        ensureStorageAccess()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val settings by themeViewModel.settings.collectAsState()
            McoderTheme(theme = settings.theme) {
                Surface {
                    McoderAppRoot()
                }
            }
        }
    }

    private fun ensureStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }
        }
    }
}
