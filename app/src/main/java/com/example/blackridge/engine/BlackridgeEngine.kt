package com.example.blackridge.engine

import com.example.blackridge.model.*
import kotlin.random.Random

class BlackridgeEngine(
    private val geminiHelper: GeminiEngineHelper = GeminiEngineHelper()
) {

    fun generateTurnOne(state: GameState): TurnOutput {
        val locationInfo = WorldData.LOCATIONS[state.locationName]
        val locStr = "${state.locationName} (${state.locationDistrict})"
        
        val scene = when (state.characterProfile.archetype) {
            "Busker" -> "Damp October air clings to your knuckles as you lean against the rain-streaked brick outside Murphy's Pub. The muffled thud of bass and laughter from inside bleeds through the frosted amber windows, while greasy steam vents hiss down Low Street Alley. A neon sign flickers overhead, buzzing like an angry hornet."
            "New Arrival" -> "The heavy iron turnstiles of the Metro Station shudder as a distant transit train departs into the tunnels. Cold, damp platform air carries the scent of ozone and burnt coffee. Fluorescent tubes flicker against water-damaged tile walls, and two transit guards with heavy batons watch commuters from the stairs."
            "Underground Fighter" -> "Oily black water laps against rotten wooden pylons beneath the Old Loading Dock. The canal fog hangs thick as wool, masking the silhouette of rusted cranes overhead. A rusted oil drum fire smolders twenty yards down the slipway, throwing twitching shadows across the wet gravel."
            else -> "${locationInfo?.shortDesc ?: "You stand in the shadows of the alley."} ${locationInfo?.ambientSounds ?: "Wind howls between derelict buildings."}"
        }

        val worldTick = when (state.turn) {
            1 -> "Bishop's cargo trucks roll past the canal checkpoints unhindered; Darius flips open his collection ledger in the back booth of Murphy's."
            else -> "Cal weighs copper wire in The Cellar; Rae rummages through a broken dumpster on Low Street."
        }

        val npcAction = when (state.locationName) {
            "Murphy's Pub" -> "Murphy leans over the worn mahogany counter, drying a mug with a grey bar towel. \"Either buy a pint or take the noise down the block, kid. Darius is inside, and he ain't in the mood for acoustic ballads tonight.\""
            "Metro Station" -> "A transit clerk behind scratched acrylic points toward the exit turnstiles. \"Gates lock at midnight. If you're looking for cheap bunks, Southside is three stops south, but keep your hands on your bag.\""
            "Old Loading Dock" -> "Cal's runner spits into the canal water and crosses his arms under a heavy oilskin jacket. \"You got ten minutes before Cal locks the basement gates. If you ain't got his collateral, don't knock.\""
            else -> "A lone hooded figure pauses under a sodium streetlamp, eye level hidden beneath a wet jacket, before turning down the cross street."
        }

        val stateChanges = "NONE"
        val pic = when (state.locationName) {
            "Murphy's Pub" -> "A cracked neon shamrock buzzing over rain-blackened asphalt as a lone busker counts damp dollar bills."
            "Metro Station" -> "Yellow safety tiles disappearing into subterranean gloom beneath flickering fluorescent lamps."
            "Old Loading Dock" -> "Rusted crane cables swaying over black canal waters veiled in industrial smog."
            else -> "Cold rain streaking down soot-stained brick walls under the orange glare of sodium vapor lights."
        }

        val actionA = when (state.locationName) {
            "Murphy's Pub" -> "Step inside Murphy's Pub and look for a warm seat"
            "Metro Station" -> "Push through the turnstiles toward the Central Plaza exit"
            "Old Loading Dock" -> "Head down the concrete stairs into The Cellar"
            else -> "Look around and examine your surroundings"
        }

        val actionB = when (state.locationName) {
            "Murphy's Pub" -> "Unpack your instrument and busk for coins near the awning"
            "Metro Station" -> "Check your prepaid phone for reception and read Sofia's texts"
            "Old Loading Dock" -> "Search the stacked cargo pallets for salvage"
            else -> "Check your pockets and count your remaining cash"
        }

        val actionC = when (state.locationName) {
            "Murphy's Pub" -> "Slip into Low Street Alley to avoid Darius's spotters"
            "Metro Station" -> "Ask the transit clerk where to find underground work"
            "Old Loading Dock" -> "Walk across Canal Bridge toward East Yards"
            else -> "Approach the nearest person and ask what's happening"
        }

        return TurnOutput(
            turnNumber = state.turn,
            dayNumber = state.day,
            weekday = state.weekday.displayName,
            timeFormatted = state.formatTime(),
            location = locStr,
            cash = state.cash,
            hunger = state.hunger.displayName,
            thirst = state.thirst.displayName,
            inventoryText = state.getInventorySummary(),
            health = state.health.displayName,
            crewRepText = state.getCrewRepSummary(),
            scene = scene,
            worldTick = worldTick,
            npcAction = npcAction,
            stateChanges = stateChanges,
            pic = pic,
            actionA = actionA,
            actionB = actionB,
            actionC = actionC,
            actionD = "freeform — type anything"
        )
    }

    suspend fun resolveTurn(
        currentState: GameState,
        playerAction: String,
        recentTurns: List<TurnOutput> = emptyList()
    ): Pair<GameState, TurnOutput> {
        // Try Gemini if API key is valid
        if (currentState.apiKey.isNotEmpty() && currentState.apiKey != "SKIP") {
            val aiTurn = geminiHelper.requestTurnFromGemini(currentState, playerAction, currentState.apiKey, recentTurns)
            if (aiTurn != null) {
                val updatedState = updateStateFromAiTurn(currentState, aiTurn, playerAction)
                return Pair(updatedState, aiTurn)
            }
        }

        // Deterministic offline procedural engine
        val (state, turn) = resolveTurnProcedurally(currentState, playerAction)
        return Pair(state, turn.copy(playerAction = playerAction))
    }

    private fun resolveTurnProcedurally(currentState: GameState, action: String): Pair<GameState, TurnOutput> {
        val nextTurnNumber = currentState.turn + 1
        val minuteAdvance = Random.nextInt(7, 9) // 7-8 min per turn
        var newMinutes = currentState.timeMinutes + minuteAdvance
        var newDay = currentState.day
        var newWeekday = currentState.weekday

        if (newMinutes >= 24 * 60) {
            newMinutes -= 24 * 60
            newDay += 1
            newWeekday = getNextWeekday(newWeekday)
        }

        var newCash = currentState.cash
        var newHunger = currentState.hunger
        var newThirst = currentState.thirst
        var newHealth = currentState.health
        var newLocationName = currentState.locationName
        var newDistrict = currentState.locationDistrict
        val newInventory = currentState.inventory.toMutableList()
        val newReputation = currentState.reputation.toMutableMap()
        val visited = currentState.visitedLocations.toMutableSet()
        val changesList = mutableListOf<String>()
        var isFirstVisit = false

        // Hunger / Thirst natural tick every ~20 turns (approx 2.5 hours)
        if (nextTurnNumber % 22 == 0) {
            if (newThirst == ThirstState.Full) {
                newThirst = ThirstState.Thirsty
                changesList.add("THIRST: Thirsty")
            } else if (newThirst == ThirstState.Thirsty) {
                newThirst = ThirstState.Dehydrated
                changesList.add("THIRST: Dehydrated (-Stamina)")
            }
        }
        if (nextTurnNumber % 32 == 0) {
            if (newHunger == HungerState.Full) {
                newHunger = HungerState.Hungry
                changesList.add("HUNGER: Hungry")
            } else if (newHunger == HungerState.Hungry) {
                newHunger = HungerState.Starving
                changesList.add("HUNGER: Starving")
            }
        }

        // Damage from extreme deprivation
        if (newThirst == ThirstState.Dehydrated && newHunger == HungerState.Starving && nextTurnNumber % 10 == 0) {
            newHealth = degradeHealth(newHealth)
            changesList.add("HEALTH: ${newHealth.displayName} (Dehydration & Starvation)")
        }

        // Parse Action Intent
        val cleanAction = action.lowercase().trim()
        val isMoving = cleanAction.contains("walk to") || cleanAction.contains("go to") ||
                cleanAction.contains("head to") || cleanAction.contains("cross") ||
                cleanAction.contains("slip into") || cleanAction.contains("step inside")

        // Destination match
        var targetLocation: LocationInfo? = null
        for ((locName, info) in WorldData.LOCATIONS) {
            if (cleanAction.contains(locName.lowercase()) ||
                cleanAction.contains(info.id.replace("_", " ")) ||
                (locName == "Murphy's Pub" && cleanAction.contains("murphy")) ||
                (locName == "The Anchor" && cleanAction.contains("anchor")) ||
                (locName == "The Cellar" && cleanAction.contains("cellar")) ||
                (locName == "24/7 Bodega" && cleanAction.contains("bodega")) ||
                (locName == "Neon Arcade" && cleanAction.contains("arcade")) ||
                (locName == "Metro Station" && cleanAction.contains("metro"))
            ) {
                targetLocation = info
                break
            }
        }

        var sceneText = ""
        var npcActionText = ""

        if (targetLocation != null && targetLocation.name != currentState.locationName) {
            newLocationName = targetLocation.name
            newDistrict = targetLocation.district
            if (!visited.contains(newLocationName)) {
                isFirstVisit = true
                visited.add(newLocationName)
                changesList.add("LOCATION: $newLocationName (First Visit)")
            } else {
                changesList.add("LOCATION: $newLocationName")
            }

            sceneText = "You make your way through the damp gloom to ${targetLocation.name}. ${targetLocation.shortDesc} ${targetLocation.ambientSounds}"
            npcActionText = generateNpcActionForLocation(targetLocation.name, cleanAction, newReputation)
        } else {
            // Action within current location
            val resolution = resolveLocalAction(
                locationName = newLocationName,
                action = cleanAction,
                currentState = currentState,
                inventory = newInventory,
                reputation = newReputation
            )

            sceneText = resolution.scene
            npcActionText = resolution.npcAction
            newCash += resolution.cashDelta
            if (resolution.cashDelta != 0) {
                changesList.add("CASH: ${if (resolution.cashDelta > 0) "+$" else "-$"}${kotlin.math.abs(resolution.cashDelta)}")
            }
            if (resolution.thirstChange != null) {
                newThirst = resolution.thirstChange
                changesList.add("THIRST: ${newThirst.displayName}")
            }
            if (resolution.hungerChange != null) {
                newHunger = resolution.hungerChange
                changesList.add("HUNGER: ${newHunger.displayName}")
            }
            if (resolution.healthChange != null) {
                newHealth = resolution.healthChange
                changesList.add("HEALTH: ${newHealth.displayName}")
            }
            resolution.itemsAdded.forEach {
                newInventory.add(it)
                changesList.add("ITEM: +${it.name}")
            }
            resolution.itemsRemoved.forEach { itemId ->
                val idx = newInventory.indexOfFirst { it.id == itemId }
                if (idx != -1) {
                    val removed = newInventory.removeAt(idx)
                    changesList.add("ITEM: -${removed.name}")
                }
            }
            if (resolution.repNote.isNotEmpty()) {
                changesList.add(resolution.repNote)
            }
        }

        // Autonomous World Tick
        val worldTickText = generateAutonomousWorldTick(nextTurnNumber, newDistrict)

        // Visual Pic 1-line
        val picText = generatePicLine(newLocationName, cleanAction)

        // Actions A, B, C
        val (actA, actB, actC) = generateContextualActions(newLocationName, cleanAction, newInventory)

        val stateChangesStr = if (changesList.isEmpty()) "NONE" else changesList.joinToString("; ")

        val isGameOver = newHealth == HealthState.Critical && cleanAction.contains("attack")
        val gameOverReason = if (isGameOver) "Succumbed to fatal wounds in Blackridge." else ""

        val updatedState = currentState.copy(
            turn = nextTurnNumber,
            day = newDay,
            weekday = newWeekday,
            timeMinutes = newMinutes,
            locationName = newLocationName,
            locationDistrict = newDistrict,
            cash = newCash.coerceAtLeast(0),
            hunger = newHunger,
            thirst = newThirst,
            health = newHealth,
            inventory = newInventory,
            reputation = newReputation,
            visitedLocations = visited,
            isGameOver = isGameOver,
            gameOverReason = gameOverReason
        )

        val turnOutput = TurnOutput(
            turnNumber = nextTurnNumber,
            dayNumber = newDay,
            weekday = newWeekday.displayName,
            timeFormatted = updatedState.formatTime(),
            location = "${updatedState.locationName} (${updatedState.locationDistrict})",
            cash = updatedState.cash,
            hunger = updatedState.hunger.displayName,
            thirst = updatedState.thirst.displayName,
            inventoryText = updatedState.getInventorySummary(),
            health = updatedState.health.displayName,
            crewRepText = updatedState.getCrewRepSummary(),
            scene = sceneText,
            worldTick = worldTickText,
            npcAction = npcActionText,
            stateChanges = stateChangesStr,
            pic = picText,
            actionA = actA,
            actionB = actB,
            actionC = actC,
            actionD = "freeform — type anything",
            isEntryRule = isFirstVisit
        )

        return Pair(updatedState, turnOutput)
    }

    private fun generateAutonomousWorldTick(turn: Int, currentDistrict: String): String {
        val ticks = listOf(
            "Darius dispatches two heavies toward the Southside tenements; a debtor's window shatters three streets over.",
            "Bishop's refrigerated freight trucks clear the East Yards gate; dock security turns away with thick envelopes.",
            "Cal tests a set of cracked radio transceivers in The Cellar; word spreads that police scanners caught a raid warning.",
            "Rae trades a tarnished lighter for three mouthfuls of cheap gin on Low Street, watching the fire escape shadows.",
            "Lena intercepts a confidential dispatch from 4th Precinct Narcotics; she lists the data drop price at $120.",
            "Sofia dials another unanswered call from the Dead End Motel payphone; the dial tone hums against the rain.",
            "Rage's gang tags the brick abutment near Canal Bridge with fresh red enamel, asserting turf over the rail line.",
            "Michel patches a deep knife laceration in the tenement basement by kerosene light, refusing to ask the boy's name."
        )
        return ticks[(turn + currentDistrict.length) % ticks.size]
    }

    private fun generateNpcActionForLocation(location: String, action: String, rep: MutableMap<String, String>): String {
        return when (location) {
            "Murphy's Pub" -> "Murphy glares over the tap handles. \"Darius is in the back booth. Keep your voice down if you value your teeth.\" In the corner, Darius taps a stainless steel ring against a pint glass."
            "The Anchor" -> "The bartender slides a cracked saucer across the zinc counter. \"First time down here? Don't look at the back tables, and cash speaks before words.\""
            "The Cellar" -> "Cal looks up from a workbench strewn with dismantled circuit boards. \"If you're buying, make it fast. If you're selling, put it on the counter where I can see it.\""
            "24/7 Bodega" -> "The clerk behind scratched plexiglass nods toward the wire shelves. \"Hot water is fifty cents. Instant noodles are three bucks. No IOUs, no exceptions.\""
            "Low Street Alley" -> "Rae pulls her coat collar up against the drizzle and rasps, \"Got a spare smoke? Or maybe you saw Darius's boys prowling near the fire escape? They're shaking people down.\""
            "Neon Arcade" -> "Lena doesn't take her eyes off her dual monitors as her fingers dance across the mechanical keyboard. \"Five minutes of my time costs twenty dollars. What frequencies are you looking for?\""
            "Dead End Motel" -> "A woman in room 14 pulls the Venetian blinds aside for a second, peering out into the gravel lot before snapping them shut."
            "Bishop's Club" -> "Two broad-shouldered guards in dark wool coats cross their arms before the mahogany doors. \"Members only. Walk away before we help you walk.\""
            "Central Plaza" -> "A city automated security drone hums past on low altitude, scanning pedestrians with a blue laser beam before drifting toward the metro."
            else -> "A passing stranger pulls their collar tight against the cold wind and quickens their pace into the mist."
        }
    }

    private data class LocalResolution(
        val scene: String,
        val npcAction: String,
        val cashDelta: Int = 0,
        val thirstChange: ThirstState? = null,
        val hungerChange: HungerState? = null,
        val healthChange: HealthState? = null,
        val itemsAdded: List<Item> = emptyList(),
        val itemsRemoved: List<String> = emptyList(),
        val repNote: String = ""
    )

    private fun resolveLocalAction(
        locationName: String,
        action: String,
        currentState: GameState,
        inventory: List<Item>,
        reputation: MutableMap<String, String>
    ): LocalResolution {
        // Busking / Performing
        if (action.contains("busk") || action.contains("play guitar") || action.contains("instrument")) {
            val earned = Random.nextInt(6, 15)
            return LocalResolution(
                scene = "You position yourself beneath the tavern overhang and tune your strings. The melancholic chords echo through the damp mist, catching the ear of shivering commuters and tavern patrons.",
                npcAction = "A factory worker tosses a handful of coins into your open guitar case. \"Good tune, kid. Better pack up before Darius notices you making money on his block.\"",
                cashDelta = earned,
                repNote = "CASH: +$$earned; Murphy: Tolerate busking for now"
            )
        }

        // Buying Food or Drink
        if (action.contains("buy food") || action.contains("eat") || action.contains("noodle") || action.contains("ration")) {
            if (currentState.cash >= 4) {
                return LocalResolution(
                    scene = "You purchase a steaming carton of spicy broth and instant noodles. The heat warms your numb fingers, and the sodium-heavy broth cuts through the evening chill.",
                    npcAction = "The vendor grunts and pockets the bills. \"Trash goes in the barrel, don't leave it on the curb.\"",
                    cashDelta = -4,
                    hungerChange = HungerState.Full,
                    thirstChange = ThirstState.Full,
                    repNote = "HUNGER: Full; THIRST: Full"
                )
            } else {
                return LocalResolution(
                    scene = "You count your remaining coins, but you come up short. The aroma of simmering broth only makes your stomach cramp harder.",
                    npcAction = "\"Four dollars flat,\" the vendor barks, pulling the carton back. \"Come back when you got the coin.\"",
                    cashDelta = 0
                )
            }
        }

        // Buying Drink / Water / Pint
        if (action.contains("drink") || action.contains("buy drink") || action.contains("pint") || action.contains("water")) {
            if (currentState.cash >= 3) {
                return LocalResolution(
                    scene = "You order a tall glass of tap water and a cheap draught. The cold liquid soothes your parched throat instantly.",
                    npcAction = "Murphy wipes the bar rail with a heavy hand. \"Drink up. We close in two hours.\"",
                    cashDelta = -3,
                    thirstChange = ThirstState.Full,
                    repNote = "THIRST: Full"
                )
            } else {
                return LocalResolution(
                    scene = "You ask for water, but Murphy gestures toward the rusted fire hydrant in the alley. \"Tavern glasses are for paying patrons.\"",
                    npcAction = "A nearby patron chuckles dryly and counts his poker chips.",
                    cashDelta = 0
                )
            }
        }

        // Interacting with Darius / Loan Shark
        if (action.contains("darius") || action.contains("loan") || action.contains("debt")) {
            return LocalResolution(
                scene = "You approach the leather booth where Darius sits under the dim pendant lamp. Smoke curls from his cigarette as his cold eyes trace your stance.",
                npcAction = "Darius smiles without warmth, tapping his collection ledger. \"Tick tock. You owe $${currentState.debtAmount}. If I don't see the full envelope by Sunday midnight, my boys take whatever is left of your fingers.\"",
                repNote = "Darius: Squeezing for full debt payment ($${currentState.debtAmount})"
            )
        }

        // Interacting with Rae / Smokes
        if (action.contains("rae") || action.contains("smoke") || action.contains("cigarette")) {
            val hasSmokes = inventory.any { it.id == "smokes" }
            if (hasSmokes) {
                return LocalResolution(
                    scene = "You flick a lit cigarette over to Rae. Her shaking hands catch it gratefully, the orange cherry illuminating her hollow cheeks as she inhales.",
                    npcAction = "Rae exhales a thick cloud into the drizzle and leans closer. \"Listen... Darius is stashing cash in the lockbox behind Murphy's storeroom. But Cal at The Cellar is looking for a delivery boy. Good coin if you're fast.\"",
                    itemsRemoved = listOf("smokes"),
                    repNote = "Rae: Grateful, shared rumor about Cal's courier job"
                )
            } else {
                return LocalResolution(
                    scene = "You talk to Rae in the alley shadows, but she shivers aggressively in the damp wind.",
                    npcAction = "Rae wraps her coat tighter. \"Nothin' in this city is free, friend. Got a smoke or a dollar? Otherwise, save your breath.\""
                )
            }
        }

        // Checking phone / Sofia / Texts
        if (action.contains("phone") || action.contains("text") || action.contains("sofia") || action.contains("voicemail")) {
            return LocalResolution(
                scene = "You pull out the cheap burner phone. The backlight casts an eerie cyan glow over your face. Three unread text messages glare on the cracked screen.",
                npcAction = "MESSAGE FROM SOFIA: \"AJ, please tell me you're alive. Darius's collectors came to my sister's place. I'm holed up at Dead End Motel, Room 14. Don't go to the yards.\"",
                repNote = "Sofia: Awaiting your arrival at Dead End Motel"
            )
        }

        // Combat / Fight / Punch / Threaten
        if (action.contains("punch") || action.contains("attack") || action.contains("fight") || action.contains("strike") || action.contains("hit")) {
            val newHp = degradeHealth(currentState.health)
            return LocalResolution(
                scene = "You lash out with raw fury. The collision is brutal and messy—bones crunch, furniture skids across the floor, and shouts erupt through the room.",
                npcAction = "Darius's heavy catches your shoulder with an iron pipe, knocking you hard against the wall before bystanders pull him back. \"Next time, you don't get up!\"",
                healthChange = newHp,
                repNote = "HEALTH: ${newHp.displayName}; Crew: Hostility spiked"
            )
        }

        // Search / Loot / Scavenge
        if (action.contains("search") || action.contains("loot") || action.contains("pockets") || action.contains("scavenge") || action.contains("look around")) {
            val foundItem = if (Random.nextBoolean()) {
                Item("scrap_metal", "Salvaged Copper Tubing", 1, "Can be sold to Cal or pawn shop.", "Valuable", 12)
            } else {
                Item("pocket_knife", "Rusty Pocket Knife", 1, "Small foldable utility knife.", "Weapon", 8)
            }
            return LocalResolution(
                scene = "You methodically inspect the shadowy corners, checking beneath discarded crates and rusted grating. The wet ground hides discarded remnants of the district's underbelly.",
                npcAction = "A scruffy stray cat darts out from behind a trash can, watching you with yellow eyes.",
                itemsAdded = listOf(foundItem),
                repNote = "ITEM: +${foundItem.name}"
            )
        }

        // Default local action
        return LocalResolution(
            scene = "You take a steady breath, surveying your surroundings. Rain drips steadily from the awnings into overflowing gutters. People hurry past, heads down, avoiding eye contact in the dim streetlights.",
            npcAction = "Someone nearby mutters about the rising prices at the bodega and the increasing police patrols near the rail lines."
        )
    }

    private fun generatePicLine(location: String, action: String): String {
        return when {
            action.contains("busk") -> "A weathered wooden guitar case collecting wet quarters under a dripping green awning."
            action.contains("darius") -> "A heavy fist with brass rings resting beside a half-empty glass of amber whiskey."
            action.contains("fight") -> "Sparks flying as boots skid over wet cobbles in a dimly illuminated alley."
            location == "Murphy's Pub" -> "Amber light spilling through frosted glass onto rainy cobblestones as a neon shamrock flickers."
            location == "The Cellar" -> "Bare overhead lightbulb swaying above an underground sparring ring wrapped in hemp rope."
            location == "Metro Station" -> "Wet platform tiles reflecting the eerie fluorescent flicker of an empty transit tunnel."
            location == "Old Loading Dock" -> "Silhouette of a rusted gantry crane towering above fog-shrouded black canal water."
            location == "Neon Arcade" -> "Rows of glowing arcade screens casting magenta and cyan geometric patterns on dirty linoleum."
            else -> "A lone figure standing beneath an orange sodium streetlamp as mist rises from a steam grate."
        }
    }

    private fun generateContextualActions(location: String, lastAction: String, inventory: List<Item>): Triple<String, String, String> {
        val locInfo = WorldData.LOCATIONS[location]
        val connected = locInfo?.connectedLocations ?: listOf("Central Plaza", "Murphy's Pub", "Low Street Alley")

        val actA = when (location) {
            "Murphy's Pub" -> "Walk to Low Street Alley and check the fire escape"
            "Low Street Alley" -> "Walk to 24/7 Bodega to buy rations or warm drink"
            "24/7 Bodega" -> "Walk back to Murphy's Pub"
            "The Cellar" -> "Talk to Cal about fencing goods or finding work"
            "Old Loading Dock" -> "Head down the stairs into The Cellar"
            "Neon Arcade" -> "Talk to Lena about hacking transit codes"
            "Metro Station" -> "Walk to Central Plaza spires"
            "Central Plaza" -> "Walk to The Anchor dive bar"
            "The Anchor" -> "Step up to the bar and order a cheap drink"
            else -> "Walk to ${connected.firstOrNull() ?: "Murphy's Pub"}"
        }

        val actB = when (location) {
            "Murphy's Pub" -> "Confront Darius about your debt terms"
            "Low Street Alley" -> "Offer Rae a cigarette in exchange for street intel"
            "24/7 Bodega" -> "Buy hot instant noodles ($4)"
            "The Cellar" -> "Step into the sparring ring to earn fight purse"
            "Old Loading Dock" -> "Search the derelict shipping crates for scrap"
            "Neon Arcade" -> "Inspect the arcade coin drawers for loose change"
            "Metro Station" -> "Check your phone and read unread texts from Sofia"
            else -> "Count your remaining cash and check your gear"
        }

        val actC = when (location) {
            "Murphy's Pub" -> "Unpack your instrument and busk near the door"
            "Low Street Alley" -> "Slip into the shadows and eavesdrop on passersby"
            "24/7 Bodega" -> "Ask the clerk who controls the protection circuit here"
            "The Cellar" -> "Walk to Old Loading Dock to watch the canal"
            "Old Loading Dock" -> "Walk across Canal Bridge toward East Yards"
            "Neon Arcade" -> "Walk to Pawn & Gold to appraise items"
            "Metro Station" -> "Ask the transit clerk about train departures"
            else -> "Walk to ${connected.getOrNull(1) ?: "Central Plaza"}"
        }

        return Triple(actA, actB, actC)
    }

    private fun degradeHealth(current: HealthState): HealthState {
        return when (current) {
            HealthState.Fine -> HealthState.Minor
            HealthState.Minor -> HealthState.Moderate
            HealthState.Moderate -> HealthState.Serious
            HealthState.Serious -> HealthState.Critical
            HealthState.Critical -> HealthState.Critical
        }
    }

    private fun getNextWeekday(day: Weekday): Weekday {
        val values = Weekday.values()
        val nextIdx = (day.ordinal + 1) % values.size
        return values[nextIdx]
    }

    private fun updateStateFromAiTurn(currentState: GameState, aiTurn: TurnOutput, action: String): GameState {
        // Use the clock the engine printed; fall back to a fixed 8-minute tick if it is unparseable
        val newMinutes = parseClockMinutes(aiTurn.timeFormatted)
            ?: ((currentState.timeMinutes + 8) % (24 * 60))

        val hunger = HungerState.values().find { it.displayName.equals(aiTurn.hunger, ignoreCase = true) } ?: currentState.hunger
        val thirst = ThirstState.values().find { it.displayName.equals(aiTurn.thirst, ignoreCase = true) } ?: currentState.thirst
        val health = HealthState.values().find { it.displayName.equals(aiTurn.health, ignoreCase = true) } ?: currentState.health

        val visited = currentState.visitedLocations.toMutableSet()
        val locName = aiTurn.location.split("(").firstOrNull()?.trim() ?: currentState.locationName
        visited.add(locName)
        val district = WorldData.LOCATIONS[locName]?.district
            ?: aiTurn.location.substringAfter("(", "").substringBefore(")").trim().ifEmpty { currentState.locationDistrict }

        return currentState.copy(
            turn = aiTurn.turnNumber,
            day = aiTurn.dayNumber,
            weekday = Weekday.values().find { it.displayName.equals(aiTurn.weekday, ignoreCase = true) } ?: currentState.weekday,
            timeMinutes = newMinutes,
            locationName = locName,
            locationDistrict = district,
            cash = aiTurn.cash,
            hunger = hunger,
            thirst = thirst,
            health = health,
            visitedLocations = visited
        )
    }

    private fun parseClockMinutes(time: String): Int? {
        val match = Regex("(\\d{1,2}):(\\d{2})\\s*(AM|PM)", RegexOption.IGNORE_CASE).find(time) ?: return null
        val hour12 = match.groupValues[1].toInt() % 12
        val minute = match.groupValues[2].toInt()
        val isPm = match.groupValues[3].equals("PM", ignoreCase = true)
        return (hour12 + if (isPm) 12 else 0) * 60 + minute
    }
}
