package com.example.blackridge.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blackridge.data.BlackridgeDatabase
import com.example.blackridge.data.GameSaveEntity
import com.example.blackridge.data.GameStateSerializer
import com.example.blackridge.data.TurnLogEntity
import com.example.blackridge.engine.BlackridgeEngine
import com.example.blackridge.engine.WorldData
import com.example.blackridge.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AppScreen {
    object StartNsfw : AppScreen()        // SCREEN 1
    object StartApi : AppScreen()         // SCREEN 2
    object StartCharacter : AppScreen()   // SCREEN 3
    object GameSession : AppScreen()      // Main Engine Terminal
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.StartNsfw,
    val nsfwSetting: NsfwSetting = NsfwSetting.OFF,
    val apiKey: String = "",
    val activeSlotId: Int = 1,
    val gameState: GameState? = null,
    val currentTurnOutput: TurnOutput? = null,
    val turnHistory: List<TurnOutput> = emptyList(),
    val isProcessingTurn: Boolean = false,
    val showMapDialog: Boolean = false,
    val showInventoryDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val freeformInputText: String = "",
    val statusMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BlackridgeDatabase.getDatabase(application)
    private val gameDao = db.gameDao()
    private val engine = BlackridgeEngine()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Check if an existing game can be loaded
        checkExistingSave()
    }

    private fun checkExistingSave() {
        viewModelScope.launch {
            val save = gameDao.getSaveBySlot(1)
            if (save != null) {
                val state = GameStateSerializer.deserialize(save.stateJson)
                if (state != null) {
                    val logs = gameDao.getTurnLogsForSlot(1)
                    val historyList = logs.mapNotNull { log ->
                        // reconstruct or provide skeleton text
                        TurnOutput(
                            turnNumber = log.turnNumber,
                            dayNumber = state.day,
                            weekday = state.weekday.displayName,
                            timeFormatted = state.formatTime(),
                            location = "${state.locationName} (${state.locationDistrict})",
                            cash = state.cash,
                            hunger = state.hunger.displayName,
                            thirst = state.thirst.displayName,
                            inventoryText = state.getInventorySummary(),
                            health = state.health.displayName,
                            crewRepText = state.getCrewRepSummary(),
                            scene = "",
                            worldTick = "",
                            npcAction = "",
                            stateChanges = "",
                            pic = "",
                            actionA = "",
                            actionB = "",
                            actionC = "",
                            rawFormattedText = log.turnSkeletonText,
                            playerAction = log.playerAction
                        )
                    }
                    val currentTurn = engine.generateTurnOne(state)
                    _uiState.update {
                        it.copy(
                            gameState = state,
                            currentTurnOutput = currentTurn,
                            turnHistory = historyList,
                            nsfwSetting = state.nsfwSetting,
                            apiKey = state.apiKey
                        )
                    }
                }
            }
        }
    }

    fun setNsfwSetting(setting: NsfwSetting) {
        _uiState.update { it.copy(nsfwSetting = setting, currentScreen = AppScreen.StartApi) }
    }

    fun setApiKeyAndProceed(key: String) {
        val trimmed = key.trim()
        _uiState.update { it.copy(apiKey = trimmed, currentScreen = AppScreen.StartCharacter) }
    }

    fun skipApiKey() {
        _uiState.update { it.copy(apiKey = "SKIP", currentScreen = AppScreen.StartCharacter) }
    }

    fun startWithPreset(presetIndex: Int) {
        val nsfw = _uiState.value.nsfwSetting
        val key = _uiState.value.apiKey
        val state = WorldData.getPresetState(presetIndex, nsfw, key)
        startGameWithState(state)
    }

    fun startWithExistingCharacter(charName: String) {
        val nsfw = _uiState.value.nsfwSetting
        val key = _uiState.value.apiKey
        val state = WorldData.getExistingCharacterState(charName, nsfw, key)
        startGameWithState(state)
    }

    fun startWithCustomCharacter(
        name: String,
        age: Int,
        sex: String,
        archetype: String,
        startingTrait: String
    ) {
        val nsfw = _uiState.value.nsfwSetting
        val key = _uiState.value.apiKey
        val profile = CharacterProfile(
            name = name.ifBlank { "Stranger" },
            age = if (age <= 0) 25 else age,
            sex = sex.ifBlank { "Unknown" },
            archetype = archetype.ifBlank { "Drifter" },
            background = "Arrived in Blackridge with a jacket, five dollars, and eyes on the exits.",
            startingTrait = startingTrait.ifBlank { "Street Smarts" }
        )
        val state = GameState(
            turn = 1,
            day = 1,
            weekday = Weekday.Friday,
            timeMinutes = 1230,
            locationDistrict = "Downtown Core",
            locationName = "The Anchor",
            cash = 25,
            hunger = HungerState.Hungry,
            thirst = ThirstState.Full,
            health = HealthState.Fine,
            inventory = listOf(
                Item("jacket", "Dark Weatherproof Jacket", 1, "Warm with deep inner pockets.", "Gear", 20),
                Item("pocket_knife", "Pocket Knife", 1, "Small sharp blade.", "Weapon", 10),
                Item("lighter", "Disposable Lighter", 1, "Flickering flint.", "Tool", 2)
            ),
            reputation = mapOf(
                "Downtown Locals" to "Newcomer, unproven",
                "Darius" to "Unaware",
                "Bishop" to "Unaware"
            ),
            visitedLocations = setOf("The Anchor"),
            characterProfile = profile,
            nsfwSetting = nsfw,
            apiKey = key,
            npcs = WorldData.INITIAL_NPCS
        )
        startGameWithState(state)
    }

    private fun startGameWithState(state: GameState) {
        viewModelScope.launch {
            val turnOne = engine.generateTurnOne(state)
            _uiState.update {
                it.copy(
                    gameState = state,
                    currentTurnOutput = turnOne,
                    turnHistory = listOf(turnOne),
                    currentScreen = AppScreen.GameSession
                )
            }
            saveStateToDb(state, turnOne, "[NEW GAME INITIALIZATION]")
            triggerHaptic(50)
        }
    }

    fun submitAction(actionText: String) {
        val curState = _uiState.value.gameState ?: return
        if (_uiState.value.isProcessingTurn) return
        val clean = actionText.trim()
        if (clean.isEmpty()) return

        _uiState.update { it.copy(isProcessingTurn = true, freeformInputText = "") }

        viewModelScope.launch {
            try {
                val (newState, newTurn) = engine.resolveTurn(curState, clean, _uiState.value.turnHistory)
                val newHistory = _uiState.value.turnHistory + newTurn

                _uiState.update {
                    it.copy(
                        gameState = newState,
                        currentTurnOutput = newTurn,
                        turnHistory = newHistory,
                        isProcessingTurn = false
                    )
                }

                saveStateToDb(newState, newTurn, clean)

                if (newTurn.stateChanges.contains("HEALTH") || newTurn.stateChanges.contains("Critical")) {
                    triggerHaptic(120)
                } else {
                    triggerHaptic(30)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingTurn = false,
                        statusMessage = "Action resolution error: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateFreeformText(text: String) {
        _uiState.update { it.copy(freeformInputText = text) }
    }

    fun toggleMap(show: Boolean) {
        _uiState.update { it.copy(showMapDialog = show) }
    }

    fun toggleInventory(show: Boolean) {
        _uiState.update { it.copy(showInventoryDialog = show) }
    }

    fun toggleHistory(show: Boolean) {
        _uiState.update { it.copy(showHistoryDialog = show) }
    }

    fun toggleSettings(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun restartGame() {
        viewModelScope.launch {
            gameDao.deleteSave(1)
            gameDao.deleteLogsForSlot(1)
            _uiState.update {
                UiState(
                    currentScreen = AppScreen.StartNsfw,
                    nsfwSetting = NsfwSetting.OFF,
                    apiKey = ""
                )
            }
        }
    }

    private suspend fun saveStateToDb(state: GameState, turnOutput: TurnOutput, action: String) {
        val stateJson = GameStateSerializer.serialize(state)
        val entity = GameSaveEntity(
            slotId = 1,
            characterName = state.characterProfile.name,
            archetype = state.characterProfile.archetype,
            turn = state.turn,
            day = state.day,
            weekday = state.weekday.displayName,
            timeFormatted = state.formatTime(),
            locationName = state.locationName,
            district = state.locationDistrict,
            cash = state.cash,
            hunger = state.hunger.displayName,
            thirst = state.thirst.displayName,
            health = state.health.displayName,
            nsfwSetting = state.nsfwSetting.name,
            apiKey = state.apiKey,
            stateJson = stateJson
        )
        gameDao.insertOrUpdateSave(entity)

        val logEntity = TurnLogEntity(
            slotId = 1,
            turnNumber = state.turn,
            playerAction = action,
            turnSkeletonText = turnOutput.toSkeletonString()
        )
        gameDao.insertTurnLog(logEntity)
    }

    private fun triggerHaptic(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
