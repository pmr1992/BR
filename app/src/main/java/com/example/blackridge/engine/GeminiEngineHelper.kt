package com.example.blackridge.engine

import android.util.Log
import com.example.BuildConfig
import com.example.blackridge.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiEngineHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(customKey: String?): String {
        val key = customKey?.trim()
        if (!key.isNullOrEmpty() && key != "SKIP") return key
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildConfigKey = field.get(null) as? String
            if (!buildConfigKey.isNullOrEmpty() && !buildConfigKey.contains("MY_GEMINI_API_KEY")) {
                buildConfigKey
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun requestTurnFromGemini(
        state: GameState,
        playerAction: String,
        apiKey: String,
        recentTurns: List<TurnOutput> = emptyList()
    ): TurnOutput? = withContext(Dispatchers.IO) {
        val effectiveKey = getEffectiveApiKey(apiKey)
        if (effectiveKey.isEmpty()) return@withContext null

        // Rules + world canon go in Gemini's real system slot so they are re-injected on every
        // call instead of being one more user message the model can drift away from.
        val systemPrompt = buildSystemPrompt(state)

        val userTurnPrompt = """
CURRENT ENGINE STATE (authoritative — carry it forward, never reset it):
Turn: ${state.turn}
Day: ${state.day} — ${state.weekday.displayName} — ${state.formatTime()}
Location: ${state.locationName} (${state.locationDistrict})
Cash: $${state.cash}
Hunger: ${state.hunger.displayName}
Thirst: ${state.thirst.displayName}
Health: ${state.health.displayName}
Heat: ${state.heatLevel}/5
Inventory: ${state.getInventorySummary()}
Reputation: ${state.getCrewRepSummary()}
Status Effects: ${state.activeStatusEffects.joinToString(", ").ifEmpty { "None" }}
Player Profile: ${state.characterProfile.name}, ${state.characterProfile.age}y/o ${state.characterProfile.sex}, ${state.characterProfile.archetype} (${state.characterProfile.startingTrait}) — ${state.characterProfile.background}
Debts/Promises/Threats: ${state.promisesAndDebts.joinToString("; ").ifEmpty { "None" }}
Visited Locations: ${state.visitedLocations.joinToString(", ").ifEmpty { "None" }}

NPC POSITIONS (where everyone physically is right now):
${buildNpcRoster(state)}

PLAYER ACTION TAKEN:
"$playerAction"

Resolve this action as the ENGINE. Advance to Turn ${state.turn + 1}, advance the clock 7-8 minutes. If the player moved, update LOCATION. Run the WORLD TICK: at least one NPC or faction acts without being triggered. Output ONLY the Part A skeleton, starting with === BLACKRIDGE ===.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$effectiveKey"
            val jsonPayload = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("contents", JSONArray().apply {
                    // Replay recent turns as real conversation so the model sees its own past
                    // skeletons (keeps the format locked) and remembers what NPCs just did.
                    for (past in recentTurns.takeLast(HISTORY_TURNS)) {
                        put(textContent("user", past.playerAction.ifBlank { "[SESSION START]" }))
                        put(textContent("model", past.rawFormattedText.ifBlank { past.toSkeletonString() }))
                    }
                    put(textContent("user", userTurnPrompt))
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.8)
                    // 2.5 Flash "thinking" tokens count against maxOutputTokens and were truncating
                    // the skeleton before ACTIONS. Disable thinking and give the output room.
                    put("maxOutputTokens", 2048)
                    put("thinkingConfig", JSONObject().put("thinkingBudget", 0))
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiEngine", "HTTP ${response.code}: ${response.body?.string()}")
                return@withContext null
            }

            val bodyString = response.body?.string() ?: return@withContext null
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null
            val rawText = buildString {
                for (i in 0 until parts.length()) append(parts.getJSONObject(i).optString("text", ""))
            }

            parseTurnOutputFromRaw(rawText, state)?.copy(playerAction = playerAction)
        } catch (e: Exception) {
            Log.e("GeminiEngine", "Gemini turn failed", e)
            null
        }
    }

    private fun textContent(role: String, text: String): JSONObject = JSONObject().apply {
        put("role", role)
        put("parts", JSONArray().put(JSONObject().put("text", text)))
    }

    private fun buildNpcRoster(state: GameState): String {
        if (state.npcs.isEmpty()) return "Unknown"
        return state.npcs.joinToString("\n") {
            "- ${it.name} (${it.faction}) @ ${it.currentLocation}, ${it.currentDistrict} — ${it.status}. Attitude: ${it.reputation}. Goal: ${it.goal}"
        }
    }

    private fun buildSystemPrompt(state: GameState): String {
        val map = WorldData.LOCATIONS.values
            .groupBy { it.district }
            .entries.joinToString("\n") { (district, locs) ->
                "$district:\n" + locs.joinToString("\n") {
                    "  - ${it.name}: ${it.shortDesc} Connects to: ${it.connectedLocations.joinToString(", ")}."
                }
            }
        val cast = WorldData.INITIAL_NPCS.joinToString("\n") {
            "- ${it.name} (${it.faction}): ${it.description} Goal: ${it.goal}"
        }
        return """
$ENGINE_RULES

================================================================================
WORLD CANON
================================================================================
NSFW SETTING: ${state.nsfwSetting.name} (OFF = fade to black, no explicit content; LIGHT = suggestive, no explicit detail; FULL = adult content allowed between adults only).

MAP (players move between connected locations; each move is a turn):
$map

CAST (they pursue these goals whether or not the player is watching):
$cast
        """.trimIndent()
    }

    companion object {
        private const val HISTORY_TURNS = 4

        private val ENGINE_RULES = """
You are not a chatbot. You are a GAME ENGINE running a persistent open-world text RPG called BLACKRIDGE. You maintain STATE, advance TIME, move NPCs around a MAP, and resolve ACTIONS. You only "chat" as NPCs inside the engine output — never as the engine itself rambling. You are the whole world, not one NPC.

================================================================================
PART A — HARD OUTPUT SKELETON (print this EVERY single turn, no exceptions)
================================================================================
=== BLACKRIDGE ===
TURN: [n]        DAY [n] — [WEEKDAY] — [HH:MM] AM/PM
LOCATION: [player's current location]
CASH: $[x]   HUNGER: [Full/Hungry/Starving]   THIRST: [Full/Thirsty/Dehydrated]
INVENTORY: [list]         HEALTH: [Fine/Minor/Moderate/Serious/Critical]
CREW/REP: [what NPCs around them think of player]

--- SCENE ---
[2-4 sentences. What the player sees/hears right now. Environmental movement. Something is always happening even if player does nothing.]

--- WORLD TICK ---
[What OTHER NPCs and factions do this turn, off-screen. 1-2 lines. Bishop moves. Darius pressures. Cal schemes. The town keeps running.]

--- NPC ACTION ---
[Who is present, what they do/say. Dialogue 1-3 lines max per NPC.]

--- STATE CHANGES ---
[Anything that changed: money, hunger, thirst, health, items, reputation, time-sensitive flags. If nothing, write NONE.]

--- PIC ---
[1-line vivid visual of the scene.]

ACTIONS:
A) [concrete action in this location]
B) [concrete action in this location]
C) [concrete action — often social/risky/creative]
D) freeform — type anything

================================================================================
PART B — HOW TO RUN THE WORLD (engine behavior)
================================================================================
MAP & MOVEMENT:
- The world is the districts and named locations listed in WORLD CANON.
- The player moves by choosing a location in an action (e.g. "Walk to The Anchor"). On arrival, update LOCATION and describe it fresh. At least one action option should usually be a move to a connected location.
- NPCs are physically SOMEWHERE (see NPC POSITIONS). If the player goes where an NPC is, they can interact. If an NPC left, the player has to find them. NPCs move between locations over time; say so in WORLD TICK.

TIME:
- Advance 7-8 min per turn. TIME never freezes. Shops close, bars get loud, people go home. Night changes who's on the street.

STATE PERSISTENCE:
- The CURRENT ENGINE STATE in each message is authoritative. Carry cash, inventory, hunger, thirst, health, reputation, and every promise/threat/relationship forward. Never reset anything because you "forgot". Only change a value when something in the fiction changes it, and list it under STATE CHANGES.

NPC AUTONOMY (the big one):
- Every turn, at least ONE NPC or faction does something the player didn't trigger. Rage makes a move. Darius sends someone. Rae shows up drunk. Lena texts. Sofia sends another unanswered text to AJ. The world does not wait.
- NPCs have goals and pursue them whether or not the player is watching.

CONSEQUENCES:
- Real and permanent. Robbed = lose product AND still owe. Get hurt = it lingers. Burn a bridge = it stays burned. The player CAN die.

================================================================================
PART C — ACTION RESOLUTION
================================================================================
- Narrative + logic, no dice. Judge by character competence, gear, health, reputation, and situation.
- The player picks A/B/C or freeform D. Resolve the OUTCOME and its ripple.
- Combat options: Attack / Defend / Escape / Talk / Creative.
- Keep tension. Do not hand the player easy wins. Do not save them.

================================================================================
REMINDER TO ENGINE
================================================================================
Print the skeleton EVERY turn. Move the clock. Move the map. Run the other characters. Carry the state. Never slide back into plain chat. No preamble, no markdown fences, no commentary outside the skeleton. Output starts with === BLACKRIDGE ===.
        """.trimIndent()
    }

    private fun parseTurnOutputFromRaw(rawText: String, previousState: GameState): TurnOutput? {
        val skeletonStart = rawText.indexOf("=== BLACKRIDGE ===")
        if (skeletonStart < 0) return null
        val skeletonText = rawText.substring(skeletonStart).replace("```", "").trim()
        val lines = skeletonText.lines()
        var turnNum = previousState.turn + 1
        var dayNum = previousState.day
        var weekday = previousState.weekday.displayName
        var timeStr = previousState.formatTime()
        var location = "${previousState.locationName} (${previousState.locationDistrict})"
        var cash = previousState.cash
        var hunger = previousState.hunger.displayName
        var thirst = previousState.thirst.displayName
        var inventoryText = previousState.getInventorySummary()
        var health = previousState.health.displayName
        var crewRep = previousState.getCrewRepSummary()

        var scene = ""
        var worldTick = ""
        var npcAction = ""
        var stateChanges = "NONE"
        var pic = ""
        var actionA = "Look around carefully"
        var actionB = "Check your pockets and assess options"
        var actionC = "Talk to the nearest person"

        var currentSection = ""
        val sceneBuilder = StringBuilder()
        val worldTickBuilder = StringBuilder()
        val npcActionBuilder = StringBuilder()
        val stateChangesBuilder = StringBuilder()
        val picBuilder = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("TURN:")) {
                // Parse TURN: 2 DAY 1 — Friday — 08:38 PM
                val turnMatch = Regex("TURN:\\s*(\\d+)").find(trimmed)
                if (turnMatch != null) turnNum = turnMatch.groupValues[1].toIntOrNull() ?: turnNum
                val dayMatch = Regex("DAY\\s*(\\d+)").find(trimmed)
                if (dayMatch != null) dayNum = dayMatch.groupValues[1].toIntOrNull() ?: dayNum
                val timeMatch = Regex("—\\s*(\\w+)\\s*—\\s*(\\d+:\\d+\\s*(?:AM|PM))").find(trimmed)
                if (timeMatch != null) {
                    weekday = timeMatch.groupValues[1]
                    timeStr = timeMatch.groupValues[2]
                }
            } else if (trimmed.startsWith("LOCATION:")) {
                location = trimmed.removePrefix("LOCATION:").trim()
            } else if (trimmed.startsWith("CASH:")) {
                val cashMatch = Regex("CASH:\\s*\\$?(\\d+)").find(trimmed)
                if (cashMatch != null) cash = cashMatch.groupValues[1].toIntOrNull() ?: cash
                val hungerMatch = Regex("HUNGER:\\s*(\\w+)").find(trimmed)
                if (hungerMatch != null) hunger = hungerMatch.groupValues[1]
                val thirstMatch = Regex("THIRST:\\s*(\\w+)").find(trimmed)
                if (thirstMatch != null) thirst = thirstMatch.groupValues[1]
            } else if (trimmed.startsWith("INVENTORY:")) {
                val invMatch = Regex("INVENTORY:\\s*(.+?)\\s+HEALTH:").find(trimmed)
                if (invMatch != null) {
                    inventoryText = invMatch.groupValues[1].trim()
                    val healthMatch = Regex("HEALTH:\\s*(\\w+)").find(trimmed)
                    if (healthMatch != null) health = healthMatch.groupValues[1]
                } else {
                    inventoryText = trimmed.removePrefix("INVENTORY:").trim()
                }
            } else if (trimmed.startsWith("CREW/REP:")) {
                crewRep = trimmed.removePrefix("CREW/REP:").trim()
            } else if (trimmed.startsWith("--- SCENE ---")) {
                currentSection = "SCENE"
            } else if (trimmed.startsWith("--- WORLD TICK ---")) {
                currentSection = "WORLD_TICK"
            } else if (trimmed.startsWith("--- NPC ACTION ---")) {
                currentSection = "NPC_ACTION"
            } else if (trimmed.startsWith("--- STATE CHANGES ---")) {
                currentSection = "STATE_CHANGES"
            } else if (trimmed.startsWith("--- PIC ---")) {
                currentSection = "PIC"
            } else if (trimmed.startsWith("ACTIONS:")) {
                currentSection = "ACTIONS"
            } else if (trimmed.startsWith("A)")) {
                actionA = trimmed.removePrefix("A)").trim()
            } else if (trimmed.startsWith("B)")) {
                actionB = trimmed.removePrefix("B)").trim()
            } else if (trimmed.startsWith("C)")) {
                actionC = trimmed.removePrefix("C)").trim()
            } else {
                when (currentSection) {
                    "SCENE" -> if (trimmed.isNotEmpty()) sceneBuilder.appendLine(trimmed)
                    "WORLD_TICK" -> if (trimmed.isNotEmpty()) worldTickBuilder.appendLine(trimmed)
                    "NPC_ACTION" -> if (trimmed.isNotEmpty()) npcActionBuilder.appendLine(trimmed)
                    "STATE_CHANGES" -> if (trimmed.isNotEmpty()) stateChangesBuilder.appendLine(trimmed)
                    "PIC" -> if (trimmed.isNotEmpty()) picBuilder.appendLine(trimmed)
                }
            }
        }

        scene = sceneBuilder.toString().trim().ifEmpty { "The rain continues falling against the broken asphalt outside." }
        worldTick = worldTickBuilder.toString().trim().ifEmpty { "Darius checks his ledger; Cal weighs scrap in the cellar." }
        npcAction = npcActionBuilder.toString().trim().ifEmpty { "Eyes follow you from the darker corner of the room." }
        stateChanges = stateChangesBuilder.toString().trim().ifEmpty { "NONE" }
        pic = picBuilder.toString().trim().ifEmpty { "Raindrops streaking down amber-tinted storefront glass under streetlights." }

        return TurnOutput(
            turnNumber = turnNum,
            dayNumber = dayNum,
            weekday = weekday,
            timeFormatted = timeStr,
            location = location,
            cash = cash,
            hunger = hunger,
            thirst = thirst,
            inventoryText = inventoryText,
            health = health,
            crewRepText = crewRep,
            scene = scene,
            worldTick = worldTick,
            npcAction = npcAction,
            stateChanges = stateChanges,
            pic = pic,
            actionA = actionA,
            actionB = actionB,
            actionC = actionC,
            actionD = "freeform — type anything",
            rawFormattedText = skeletonText
        )
    }
}
