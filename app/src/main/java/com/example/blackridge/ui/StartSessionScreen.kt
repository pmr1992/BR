package com.example.blackridge.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.blackridge.engine.AiEngineHelper
import com.example.blackridge.model.NsfwSetting
import com.example.ui.theme.*

@Composable
fun StartSessionScreen(
    viewModel: GameViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlackridgeObsidian)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (uiState.currentScreen) {
            is AppScreen.StartNsfw -> ScreenOneNsfw(
                onSelectSetting = { viewModel.setNsfwSetting(it) }
            )
            is AppScreen.StartApi -> ScreenTwoApi(
                currentKey = uiState.apiKey,
                currentModel = uiState.aiModel,
                onProceed = { key, model -> viewModel.setApiKeyAndProceed(key, model) },
                onSkip = { viewModel.skipApiKey() }
            )
            is AppScreen.StartCharacter -> ScreenThreeBegin(
                onSelectPreset = { viewModel.startWithPreset(it) },
                onSelectExisting = { viewModel.startWithExistingCharacter(it) },
                onCreateCustom = { name, age, sex, arch, trait ->
                    viewModel.startWithCustomCharacter(name, age, sex, arch, trait)
                }
            )
            else -> {}
        }
    }
}

// ============================================================================
// SCREEN 1 — NSFW SETTING: ( ) OFF   ( ) LIGHT   ( ) FULL
// ============================================================================
@Composable
private fun ScreenOneNsfw(
    onSelectSetting: (NsfwSetting) -> Unit
) {
    var selected by remember { mutableStateOf(NsfwSetting.OFF) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "=== BLACKRIDGE ===",
            style = MaterialTheme.typography.headlineLarge,
            color = AmberTerminal,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SESSION INITIALIZATION",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "SCREEN 1 — CONTENT MATURITY",
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberTerminalBright
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select content filter intensity for gritty noir violence, language, and underworld elements:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                NsfwOptionRow(
                    title = "OFF",
                    subtitle = "Standard noir tension, censored explicit profanity and extreme violence.",
                    isSelected = selected == NsfwSetting.OFF,
                    onClick = { selected = NsfwSetting.OFF }
                )

                Spacer(modifier = Modifier.height(10.dp))

                NsfwOptionRow(
                    title = "LIGHT",
                    subtitle = "Gritty urban reality, raw street dialogue, moderate underworld grit.",
                    isSelected = selected == NsfwSetting.LIGHT,
                    onClick = { selected = NsfwSetting.LIGHT }
                )

                Spacer(modifier = Modifier.height(10.dp))

                NsfwOptionRow(
                    title = "FULL",
                    subtitle = "Uncompromising hard-boiled noir, full violence tiers and uncensored street language.",
                    isSelected = selected == NsfwSetting.FULL,
                    onClick = { selected = NsfwSetting.FULL }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { onSelectSetting(selected) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_nsfw_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberTerminal,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "CONTINUE TO SCREEN 2 >>",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NsfwOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) BlackridgeSurfaceElevated else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AmberTerminal else BlackridgeBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AmberTerminal,
                unselectedColor = TextDark
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) AmberTerminalBright else TextWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

// ============================================================================
// SCREEN 2 — API (optional):
// "Paste a key for scene images / richer memory, or type SKIP to run fully offline."
// ============================================================================
@Composable
private fun ScreenTwoApi(
    currentKey: String,
    currentModel: String,
    onProceed: (String, String) -> Unit,
    onSkip: () -> Unit
) {
    var keyInput by remember { mutableStateOf(if (currentKey == "SKIP") "" else currentKey) }
    var modelInput by remember { mutableStateOf(currentModel.ifBlank { AiEngineHelper.DEFAULT_OPENROUTER_MODEL }) }
    val isOpenRouter = AiEngineHelper.isOpenRouterKey(keyInput)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "=== BLACKRIDGE ===",
            style = MaterialTheme.typography.headlineLarge,
            color = AmberTerminal,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SCREEN 2 — AI ENGINE & OFFLINE MODE",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "API Key",
                        tint = AmberTerminal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AI ENGINE / OFFLINE TOGGLE",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "\"Paste a key for scene images / richer memory, or type SKIP to run fully offline.\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberTerminalBright,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "If no key is provided, the PIC section renders as vivid text scenes. The game engine is 100% playable offline with autonomous NPCs, living world ticks, and consequence resolution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    label = { Text("OpenRouter or Gemini API Key (Optional)", color = TextMuted) },
                    placeholder = { Text("sk-or-... / AIzaSy... or leave blank to SKIP", color = TextDark) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberTerminal,
                        unfocusedBorderColor = BlackridgeBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (isOpenRouter) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_model_input"),
                        label = { Text("OpenRouter Model", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberTerminal,
                            unfocusedBorderColor = BlackridgeBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AiEngineHelper.OPENROUTER_PRESETS.forEach { (label, id) ->
                            OutlinedButton(
                                onClick = { modelInput = id },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (modelInput == id) AmberTerminal else TextMuted
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (keyInput.isNotBlank()) {
            Button(
                onClick = { onProceed(keyInput, if (isOpenRouter) modelInput else "") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_api_key_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberTerminal,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CONNECT ENGINE & PROCEED >>",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("skip_offline_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyanNeon
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(CyanNeon)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "SKIP — RUN FULLY OFFLINE (RECOMMENDED)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// SCREEN 3 — BEGIN:
//   > PRESET: 1) Busker Debt  2) AJ's First Week  3) Jax's Debt
//   > PLAY AS EXISTING (Riot / Jax / AJ / Michel / others)
//   > CREATE CHARACTER (name, age, sex required)
//   > CREATE BASED ON CHARACTER (NPC as template)
// ============================================================================
@Composable
private fun ScreenThreeBegin(
    onSelectPreset: (Int) -> Unit,
    onSelectExisting: (String) -> Unit,
    onCreateCustom: (String, Int, String, String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Presets, 1: Existing, 2: Create New

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "=== BLACKRIDGE ===",
            style = MaterialTheme.typography.titleLarge,
            color = AmberTerminal,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "SCREEN 3 — SELECT YOUR ENTRY INTO THE CITY",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BlackridgeSurface,
            contentColor = AmberTerminal,
            divider = { HorizontalDivider(color = BlackridgeBorder) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("PRESETS (1-3)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("EXISTING") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("CUSTOM") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> PresetsTab(onSelectPreset)
                1 -> ExistingCharactersTab(onSelectExisting)
                2 -> CustomCharacterTab(onCreateCustom)
            }
        }
    }
}

@Composable
private fun PresetsTab(onSelectPreset: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PresetCard(
            number = 1,
            title = "BUSKER DEBT",
            district = "Southside Flats (Murphy's Pub)",
            summary = "Cash: $18. Guitar in hand. Trapped by Darius's $450 debt deadline due Sunday midnight. You have 48 hours to find money before his heavies take your fingers.",
            gear = "Acoustic Guitar, Smokes, Lighter, $18",
            onClick = { onSelectPreset(1) }
        )

        PresetCard(
            number = 2,
            title = "AJ'S FIRST WEEK",
            district = "Downtown Core (Metro Station)",
            summary = "Cash: $40. Fresh off the late bus with a cheap prepaid phone buzzing with unread messages from Sofia. Fleeing upstate debts and trying to find shelter.",
            gear = "Prepaid Phone, Folding Knife, Duffle, $40",
            onClick = { onSelectPreset(2) }
        )

        PresetCard(
            number = 3,
            title = "JAX'S DEBT",
            district = "Old Mill District (Old Loading Dock)",
            summary = "Cash: $12. Bruised ribs and bloody knuckles. Cal is demanding collateral in the cellar pits, and Rage's gang is scouring the rail depot for you.",
            gear = "Heavy Brass Knuckles, Athletic Tape, Zippo, $12",
            onClick = { onSelectPreset(3) }
        )
    }
}

@Composable
private fun PresetCard(
    number: Int,
    title: String,
    district: String,
    summary: String,
    gear: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("preset_${number}_card"),
        colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRESET $number: $title",
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberTerminalBright
                )
                Badge(containerColor = BlackridgeSurfaceElevated) {
                    Text(district, color = CyanNeon, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "STARTING GEAR: $gear",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberTerminal, contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("LAUNCH AS PRESET $number >>", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExistingCharactersTab(onSelect: (String) -> Unit) {
    val characters = listOf(
        Triple("Riot", "Underground Courier (Downtown Core)", "Agile package runner moving sensitive contraband between Neon Arcade and the docks. Armed with lockpicks and a switchblade."),
        Triple("Jax", "Underground Fighter (Old Mill District)", "Veteran street brawler indebted to bookies. Works the underground pits beneath the textile mills."),
        Triple("AJ", "Runaway Drifter (Downtown Core)", "New arrival evading loan sharks; Sofia's distress calls remain unanswered on his burner phone."),
        Triple("Michel", "Underground Medic (Southside Flats)", "Former hospital doctor operating a hush-hush walkup clinic in Tenement Block B."),
        Triple("Rae", "Alley Survivor (Southside Flats)", "Fierce, shivering alley regular who knows every pipe, lockbox, and rumor between Murphy's and the river.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        characters.forEach { (name, role, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name.uppercase(), style = MaterialTheme.typography.titleMedium, color = CyanNeon)
                        Text(role, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = TextWhite)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { onSelect(name) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberTerminal),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AmberTerminal))
                    ) {
                        Text("PLAY AS $name", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomCharacterTab(
    onCreate: (name: String, age: Int, sex: String, archetype: String, trait: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("25") }
    var sex by remember { mutableStateOf("Male") }
    var archetype by remember { mutableStateOf("Drifter") }
    var trait by remember { mutableStateOf("Street Smarts") }

    val archetypes = listOf("Drifter", "Enforcer", "Busker", "Hustler", "Mechanic", "Medic")
    val traits = listOf("Street Smarts", "Quick Hands", "Iron Jaw", "Silver Tongue", "Silent Step")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CREATE NEW CHARACTER",
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberTerminalBright
                )
                Text(
                    text = "Name, age, and sex are required to establish your profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Character Name *") },
                    placeholder = { Text("e.g. Vance, Corina, Dex") },
                    modifier = Modifier.fillMaxWidth().testTag("custom_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberTerminal,
                        unfocusedBorderColor = BlackridgeBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Age *") },
                        modifier = Modifier.weight(1f).testTag("custom_age_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberTerminal,
                            unfocusedBorderColor = BlackridgeBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    OutlinedTextField(
                        value = sex,
                        onValueChange = { sex = it },
                        label = { Text("Sex / Gender *") },
                        modifier = Modifier.weight(1.5f).testTag("custom_sex_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberTerminal,
                            unfocusedBorderColor = BlackridgeBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("ARCHETYPE:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    archetypes.take(3).forEach { arch ->
                        FilterChip(
                            selected = archetype == arch,
                            onClick = { archetype = arch },
                            label = { Text(arch, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberTerminal,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    archetypes.drop(3).forEach { arch ->
                        FilterChip(
                            selected = archetype == arch,
                            onClick = { archetype = arch },
                            label = { Text(arch, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberTerminal,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("CORE TRAIT:", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    traits.take(3).forEach { tr ->
                        FilterChip(
                            selected = trait == tr,
                            onClick = { trait = tr },
                            label = { Text(tr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val ageInt = ageStr.toIntOrNull() ?: 24
                        onCreate(name, ageInt, sex, archetype, trait)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("create_character_button"),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberTerminal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("START TURN 1 AS $name >>", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
