package com.example.blackridge.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.blackridge.engine.WorldData
import com.example.blackridge.model.*
import com.example.ui.theme.*

// ============================================================================
// DISTRICT MAP DIALOG
// ============================================================================
@Composable
fun DistrictMapDialog(
    gameState: GameState,
    onDismiss: () -> Unit,
    onFastTravel: (String) -> Unit
) {
    val districts = listOf(
        "Downtown Core",
        "Southside Flats",
        "Old Mill District",
        "East Yards",
        "Outer Edge"
    )
    var selectedDistrict by remember { mutableStateOf(gameState.locationDistrict) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberTerminal)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BLACKRIDGE CITY MAP",
                            style = MaterialTheme.typography.titleMedium,
                            color = AmberTerminalBright
                        )
                        Text(
                            text = "CURRENT: ${gameState.locationName} (${gameState.locationDistrict})",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanNeon
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_map_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // District selector pills
                ScrollableTabRow(
                    selectedTabIndex = districts.indexOf(selectedDistrict).coerceAtLeast(0),
                    containerColor = BlackridgeSurfaceElevated,
                    contentColor = AmberTerminal,
                    edgePadding = 4.dp
                ) {
                    districts.forEachIndexed { index, dist ->
                        Tab(
                            selected = selectedDistrict == dist,
                            onClick = { selectedDistrict = dist },
                            text = {
                                val isCurrent = gameState.locationDistrict == dist
                                Text(
                                    text = if (isCurrent) "● $dist" else dist,
                                    color = if (isCurrent) AmberTerminal else TextWhite,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val subLocations = WorldData.LOCATIONS.values.filter { it.district == selectedDistrict }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subLocations) { loc ->
                        val isHere = gameState.locationName == loc.name
                        val npcsPresent = gameState.npcs.filter { it.currentLocation == loc.name }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isHere) 1.5.dp else 0.5.dp,
                                    color = if (isHere) AmberTerminal else BlackridgeBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (!isHere) {
                                        onFastTravel("Walk to ${loc.name}")
                                        onDismiss()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHere) BlackridgeSurfaceElevated else BlackridgeObsidian
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isHere) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Current",
                                                tint = AmberTerminal,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = loc.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (isHere) AmberTerminalBright else TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!isHere) {
                                        Text(
                                            text = "TRAVEL >>",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyanNeon
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = loc.shortDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                if (npcsPresent.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "NPC",
                                            tint = CautionOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SPOTTED: " + npcsPresent.joinToString(", ") { "${it.name} (${it.faction})" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CautionOrange,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// INVENTORY & VITALS DIALOG
// ============================================================================
@Composable
fun InventoryDialog(
    gameState: GameState,
    onDismiss: () -> Unit,
    onUseItem: (Item) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberTerminal)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INVENTORY & CHARACTER SHEET",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberTerminalBright
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_inv_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                    }
                }

                // Vitals banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlackridgeSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        VitalPill("CASH", "$${gameState.cash}", AmberTerminal)
                        VitalPill("HEALTH", gameState.health.displayName, if (gameState.health == HealthState.Fine) PhosphorGreen else BloodRed)
                        VitalPill("HUNGER", gameState.hunger.displayName, if (gameState.hunger == HungerState.Full) PhosphorGreen else CautionOrange)
                        VitalPill("THIRST", gameState.thirst.displayName, if (gameState.thirst == ThirstState.Full) PhosphorGreen else CautionOrange)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ITEMS IN POCKETS & BAGS:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (gameState.inventory.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Pockets are completely empty.", color = TextDark)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(gameState.inventory) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BlackridgeObsidian),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (item.quantity > 1) "${item.name} x${item.quantity}" else item.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (item.description.isNotEmpty()) {
                                            Text(
                                                text = item.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = "Category: ${item.category} • Est. Value: $${item.value}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextDark
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onUseItem(item)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberTerminal),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("USE", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Debts & Promises
                if (gameState.promisesAndDebts.isNotEmpty()) {
                    Text("ACTIVE DEBTS & PROMISES:", style = MaterialTheme.typography.labelSmall, color = CautionOrange)
                    gameState.promisesAndDebts.forEach { debt ->
                        Text("• $debt", style = MaterialTheme.typography.bodySmall, color = TextWhite)
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

// ============================================================================
// TURN HISTORY DIALOG
// ============================================================================
@Composable
fun HistoryDialog(
    history: List<TurnOutput>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(containerColor = BlackridgeSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberTerminal)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SESSION TRANSCRIPT LOG",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberTerminalBright
                    )
                    Row {
                        IconButton(onClick = {
                            val allText = history.joinToString("\n\n") { it.toSkeletonString() }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Blackridge Log", allText))
                            Toast.makeText(context, "Transcript copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Transcript", tint = CyanNeon)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { turn ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BlackridgeObsidian),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BlackridgeBorder))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "TURN ${turn.turnNumber} — ${turn.timeFormatted} (${turn.location})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberTerminal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = turn.scene.ifEmpty { turn.rawFormattedText.take(150) },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
