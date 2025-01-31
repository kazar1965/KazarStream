package com.example.kazarstream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.kazarstream.data.Channel
import com.example.kazarstream.data.M3UParser
import com.example.kazarstream.data.PreferencesManager
import com.example.kazarstream.ui.screens.HomeScreen
import com.example.kazarstream.ui.theme.KazarStreamTheme
import com.example.kazarstream.utils.Logger
import com.example.kazarstream.ui.components.DisclaimerDialog
import com.example.kazarstream.ui.components.ErrorDialog
import com.example.kazarstream.ui.components.LoadingIndicator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)

        lifecycleScope.launch {
            // Precarga la última URL
            val lastUrl = preferencesManager.lastUrl.firstOrNull()
            Logger.d("Last URL loaded: $lastUrl")
        }

        setContent {
            KazarStreamTheme {
                var showDisclaimer by remember { mutableStateOf(true) }
                var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                // Cargar preferencias al inicio
                LaunchedEffect(Unit) {
                    // Verificar si el aviso fue aceptado
                    preferencesManager.disclaimerAccepted.firstOrNull()?.let { accepted ->
                        showDisclaimer = !accepted
                    }

                    // Cargar la última URL
                    preferencesManager.lastUrl.firstOrNull()?.let { url ->
                        withContext(Dispatchers.IO) {
                            try {
                                isLoading = true
                                val loadedChannels = M3UParser.parse(url)
                                withContext(Dispatchers.Main) {
                                    channels = loadedChannels
                                }
                            } catch (e: Exception) {
                                Logger.e("Error loading channels", e)
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Error al cargar la lista: ${e.message}"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }

                if (showDisclaimer) {
                    DisclaimerDialog(
                        onAccept = {
                            scope.launch {
                                preferencesManager.setDisclaimerAccepted(true)
                                showDisclaimer = false
                            }
                        },
                        onDismiss = { finish() }
                    )
                }

                if (isLoading) {
                    LoadingIndicator()
                }

                errorMessage?.let { message ->
                    ErrorDialog(
                        message = message,
                        onDismiss = { errorMessage = null }
                    )
                }

                HomeScreen(
                    channels = channels,
                    onUrlSubmit = { _, url ->
                        scope.launch(Dispatchers.IO) {
                            try {
                                isLoading = true
                                preferencesManager.saveUrl(url)
                                val loadedChannels = M3UParser.parse(url)
                                withContext(Dispatchers.Main) {
                                    channels = loadedChannels
                                }
                            } catch (e: Exception) {
                                Logger.e("Error loading channels", e)
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Error al cargar la lista: ${e.message}"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )
            }
        }
    }
}