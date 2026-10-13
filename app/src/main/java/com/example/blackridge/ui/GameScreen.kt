package com.example.blackridge.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackridge.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val turn = uiState.currentTurnOutput
    val state = uiState.gameState
    val scrollState = rememberScrollState()

    // Auto scroll down when new turn arrives
    LaunchedEffect(turn?.turnNumber) {
        scrollState.animateScrollTo(0)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(BlackridgeObsidian),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BLACKRIDGE",
                            style = MaterialTheme.typography.titleLarge,
                            color = AmberTerminal,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (state != null) {
                            Badge(containerColor = BlackridgeSurfaceElevated) {
                                Text(
                                    text = "T:${state.turn}",
                                    color = CyanNeon,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMap(true) },
                        modifier = Modifier.testTag("map_button")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "City Map", tint = CyanNeon)
                    }
                    IconButton(
                        onClick = { viewModel.toggleInventory(true) },
                        modifier = Modifier.testTag("inventory_button")
                    ) {
                        Icon(Icons.Default.Backpack, contentDescription = "Inventory", tint = AmberTerminal)
                    }
                    IconButton(
                        onClick = { viewModel.toggleHistory(true) },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History Log", tint = TextWhite)
                    }
                    IconButton(
                        onClick = { viewModel.restartGame() },
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Restart", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlackridgeSurface,
                    titleContentColor = AmberTerminal
                )
            )
        },
        containerColor = BlackridgeObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // HUD Status Strip
            if (state != null) {
                QuickHudStrip(state = state)
            }

            // Main Turn Skeleton Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (turn != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (state != null) {
                            SceneView(state = state, turn = turn)
                        }

                        StoryCards(turn = turn)

                        // Full engine readout, one tap away
                        var showReadout by rememberSaveable { mutableStateOf(false) }
                        TextButton(
                            onClick = { showReadout = !showReadout },
                            modifier = Modifier.testTag("toggle_readout_button")
                        ) {
                            Text(
                                text = if (showReadout) "HIDE ENGINE READOUT" else "SHOW ENGINE READOUT",
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                        if (showReadout) {
                            HardSkeletonCard(turn = turn, isProcessing = uiState.isProcessingTurn)
                        }

                        // Action Selection Block
                        ActionsCard(
                            turn = turn,
                            isProcessing = uiState.isProcessingTurn,
                            freeformText = uiState.freeformInputText,
                            onFreeformChange = { viewModel.updateFreeformText(it) },
                            onSelectAction = { viewModel.submitAction(it) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AmberTerminal)
                    }
                }

                // Processing overlay banner
                if (uiState.isProcessingTurn) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlackridgeSurface.copy(alpha = 0.9f))
                            .padding(10.dp)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AmberTerminal,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ENGINE ADVANCING TIME & WORLD TICKS...",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberTerminalBright,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // Modals
    if (uiState.showMapDialog && state != null) {
        DistrictMapDialog(
            gameState = state,
            onDismiss = { viewModel.toggleMap(false) },
            onFastTravel = { viewModel.submitAction(it) }
        )
    }

    if (uiState.showInventoryDialog && state != null) {
        InventoryDialog(
            gameState = state,
            onDismiss = { viewModel.toggleInventory(false) },
            onUseItem = { item -> viewModel.submitAction("Use ${item.name}") }
        )
    }

    if (uiState.showHistoryDialog) {
        HistoryDialog(
            history = uiState.turnHistory,
            onDismiss = { viewModel.toggleHistory(false) }
        )
    }
}

// ============================================================================
// HUD QUICK STRIP
// ============================================================================
@Composable
private fun QuickHudStrip(state: GameState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BlackridgeSurfaceElevated,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cash Chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CASH: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$${state.cash}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberTerminalBright,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Health Chip
            val healthColor = when (state.health) {
                HealthState.Fine -> PhosphorGreen
                HealthState.Minor -> CyanNeon
                HealthState.Moderate -> CautionOrange
                HealthState.Serious, HealthState.Critical -> BloodRed
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HP: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = state.health.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = healthColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Hunger & Thirst
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "H:${state.hunger.displayName.take(1)} / T:${state.thirst.displayName.take(1)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.hunger == HungerState.Starving || state.thirst == ThirstState.Dehydrated) BloodRed else TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Location
            Text(
                text = state.locationDistrict,
                style = MaterialTheme.typography.labelSmall,
                color = CyanNeon,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

// ============================================================================
// PART A HARD SKELETON RENDERER
// ============================================================================
@Composable
private fun HardSkeletonCard(
    turn: TurnOutput,
    isProcessing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AmberTerminal.copy(alpha = 0.8f))
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Skeleton Header
            Text(
                text = "=== BLACKRIDGE ===",
                style = MaterialTheme.typography.titleMedium,
                color = AmberTerminal,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // TURN & DAY & TIME
            Text(
                text = "TURN: ${turn.turnNumber}        DAY ${turn.dayNumber} — ${turn.weekday} — ${turn.timeFormatted}",
                style = MaterialTheme.typography.labelMedium,
                color = TextWhite,
                fontFamily = FontFamily.Monospace
            )

            // LOCATION
            Text(
                text = "LOCATION: ${turn.location}",
                style = MaterialTheme.typography.labelMedium,
                color = CyanNeon,
                fontFamily = FontFamily.Monospace
            )

            // CASH, HUNGER, THIRST
            Text(
                text = "CASH: $${turn.cash}   HUNGER: ${turn.hunger}   THIRST: ${turn.thirst}",
                style = MaterialTheme.typography.labelSmall,
                color = TextWhite,
                fontFamily = FontFamily.Monospace
            )

            // INVENTORY & HEALTH
            Text(
                text = "INVENTORY: ${turn.inventoryText}         HEALTH: ${turn.health}",
                style = MaterialTheme.typography.labelSmall,
                color = TextWhite,
                fontFamily = FontFamily.Monospace
            )

            // CREW / REP
            Text(
                text = "CREW/REP: ${turn.crewRepText}",
                style = MaterialTheme.typography.labelSmall,
                color = AmberTerminalBright,
                fontFamily = FontFamily.Monospace
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = BlackridgeBorder
            )

            // --- SCENE ---
            SectionBlock(
                title = "--- SCENE ---",
                titleColor = AmberTerminalBright,
                content = turn.scene
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- WORLD TICK ---
            SectionBlock(
                title = "--- WORLD TICK ---",
                titleColor = CyanNeon,
                content = turn.worldTick,
                isMonospace = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- NPC ACTION ---
            SectionBlock(
                title = "--- NPC ACTION ---",
                titleColor = CautionOrange,
                content = turn.npcAction
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- STATE CHANGES ---
            val isNone = turn.stateChanges.trim().equals("NONE", ignoreCase = true)
            SectionBlock(
                title = "--- STATE CHANGES ---",
                titleColor = if (isNone) TextMuted else PhosphorGreen,
                content = turn.stateChanges,
                isMonospace = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- PIC ---
            SectionBlock(
                title = "--- PIC ---",
                titleColor = TextMuted,
                content = "[ ${turn.pic} ]",
                isMonospace = true
            )
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    titleColor: Color,
    content: String,
    isMonospace: Boolean = false
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = titleColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = content,
            style = if (isMonospace) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = TextWhite,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            lineHeight = 20.sp
        )
    }
}

// ============================================================================
// STORY CARDS — the turn as readable pieces instead of one text block
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoryCards(turn: TurnOutput) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = turn.scene,
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite,
            lineHeight = 24.sp
        )

        // Each NPC line as a speech bubble
        turn.npcAction.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = CautionOrange,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                        .background(BlackridgeSurfaceElevated)
                        .border(1.dp, CautionOrange.copy(alpha = 0.35f), RoundedCornerShape(topStart = 2.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(line, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                }
            }
        }

        if (turn.worldTick.isNotBlank()) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Public, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Meanwhile… ${turn.worldTick}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanNeon.copy(alpha = 0.85f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // State changes as chips
        val changes = turn.stateChanges.split('\n', ';', ',')
            .map { it.trim().trimStart('-', '•', ' ') }
            .filter { it.isNotEmpty() && !it.equals("NONE", ignoreCase = true) }
        if (changes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                changes.forEach { change ->
                    val bad = Regex("-\\$|lost|lose|injur|hurt|owe|heat|damage|stole|serious|critical", RegexOption.IGNORE_CASE).containsMatchIn(change)
                    val tint = if (bad) BloodRed else PhosphorGreen
                    Text(
                        text = change,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = tint,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(tint.copy(alpha = 0.12f))
                            .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// ACTIONS BLOCK (A, B, C, D)
// ============================================================================
@Composable
private fun ActionsCard(
    turn: TurnOutput,
    isProcessing: Boolean,
    freeformText: String,
    onFreeformChange: (String) -> Unit,
    onSelectAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIONS:",
                    style = MaterialTheme.typography.titleSmall,
                    color = AmberTerminal,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (turn.isEntryRule) {
                    Badge(containerColor = CautionOrange) {
                        Text("ENTRY RULE ACTIVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action A
            ActionButton(
                label = "A",
                text = turn.actionA,
                enabled = !isProcessing,
                onClick = { onSelectAction(turn.actionA) },
                testTag = "action_a_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action B
            ActionButton(
                label = "B",
                text = turn.actionB,
                enabled = !isProcessing,
                onClick = { onSelectAction(turn.actionB) },
                testTag = "action_b_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action C
            ActionButton(
                label = "C",
                text = turn.actionC,
                enabled = !isProcessing,
                onClick = { onSelectAction(turn.actionC) },
                testTag = "action_c_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action D: Freeform
            Text(
                text = "D) freeform — type anything:",
                style = MaterialTheme.typography.labelSmall,
                color = CyanNeon,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick suggestion chips
            val quickChips = listOf(
                "Look around carefully",
                "Check pockets and count cash",
                "Confront Darius about debt",
                "Buy hot food and water",
                "Rest in shadows (15 min)"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickChips) { chip ->
                    SuggestionChip(
                        onClick = { onSelectAction(chip) },
                        label = { Text(chip, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = BlackridgeSurfaceElevated,
                            labelColor = TextWhite
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = BlackridgeBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Freeform Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = freeformText,
                    onValueChange = onFreeformChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("freeform_action_input"),
                    placeholder = {
                        Text(
                            "Type action or dialogue...",
                            color = TextDark,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    enabled = !isProcessing,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberTerminal,
                        unfocusedBorderColor = BlackridgeBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = BlackridgeObsidian,
                        unfocusedContainerColor = BlackridgeObsidian
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (freeformText.isNotBlank()) onSelectAction(freeformText)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (freeformText.isNotBlank()) onSelectAction(freeformText)
                    },
                    enabled = freeformText.isNotBlank() && !isProcessing,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (freeformText.isNotBlank()) AmberTerminal else BlackridgeSurfaceElevated)
                        .testTag("submit_freeform_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Action",
                        tint = if (freeformText.isNotBlank()) Color.Black else TextDark
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, BlackridgeBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        color = BlackridgeSurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Badge(
                containerColor = AmberTerminal,
                contentColor = Color.Black
            ) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AmberTerminal,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
