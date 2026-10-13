package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blackridge.ui.AppScreen
import com.example.blackridge.ui.GameScreen
import com.example.blackridge.ui.GameViewModel
import com.example.blackridge.ui.StartSessionScreen
import com.example.ui.theme.BlackridgeObsidian
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BlackridgeObsidian
                ) {
                    BlackridgeApp()
                }
            }
        }
    }
}

@Composable
fun BlackridgeApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button for sub-screens
    BackHandler(enabled = uiState.currentScreen !is AppScreen.StartNsfw) {
        when {
            uiState.showMapDialog -> viewModel.toggleMap(false)
            uiState.showInventoryDialog -> viewModel.toggleInventory(false)
            uiState.showHistoryDialog -> viewModel.toggleHistory(false)
            uiState.currentScreen is AppScreen.StartApi -> {
                // Return to Screen 1
                viewModel.restartGame()
            }
            uiState.currentScreen is AppScreen.StartCharacter -> {
                // Return to Screen 2
                viewModel.setNsfwSetting(uiState.nsfwSetting)
            }
            uiState.currentScreen is AppScreen.GameSession -> {
                // Stay on terminal, or could prompt exit
            }
        }
    }

    when (uiState.currentScreen) {
        is AppScreen.GameSession -> {
            GameScreen(viewModel = viewModel, uiState = uiState)
        }
        else -> {
            StartSessionScreen(viewModel = viewModel, uiState = uiState)
        }
    }
}
