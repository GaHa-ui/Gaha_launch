package com.mcoder.presentation.web

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * WebView screen for local and remote agent UIs.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebScreen(
    onBack: () -> Unit,
    viewModel: WebViewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activeUrl by viewModel.activeUrl.collectAsState()
    val manualUrl by viewModel.manualUrl.collectAsState()
    val token by viewModel.token.collectAsState()
    val userSettings by viewModel.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "WebView", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = manualUrl,
            onValueChange = viewModel::updateManualUrl,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL (локальный или удаленный)") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = token,
            onValueChange = viewModel::updateToken,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Токен авторизации") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = viewModel::autoDetect) {
                Text("Автодетект")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onBack) {
                Text("Назад")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = userSettings.webViewJsEnabled
                    settings.domStorageEnabled = userSettings.webViewLocalStorageEnabled
                    loadUrl(activeUrl)
                }
            },
            update = { webView ->
                webView.settings.javaScriptEnabled = userSettings.webViewJsEnabled
                webView.settings.domStorageEnabled = userSettings.webViewLocalStorageEnabled
                if (webView.url != activeUrl) {
                    webView.loadUrl(activeUrl)
                }
            }
        )
    }
}
