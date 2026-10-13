package com.example.blackridge.engine

import com.example.blackridge.model.*

object WorldData {

    val LOCATIONS: Map<String, LocationInfo> = mapOf(
        // Downtown Core
        "The Anchor" to LocationInfo(
            id = "the_anchor",
            district = "Downtown Core",
            name = "The Anchor",
            shortDesc = "A dimly lit subterranean dive bar beneath a neon anchor sign. The air smells of spilled stout and cheap cigars.",
            ambientSounds = "Jukebox static, ice clinking, muted whispers from leather booths.",
            connectedLocations = listOf("Central Plaza", "Neon Arcade", "Metro Station")
        ),
        "Central Plaza" to LocationInfo(
            id = "central_plaza",
            district = "Downtown Core",
            name = "Central Plaza",
            shortDesc = "Towering concrete and glass office spires surrounding a plaza swept by rain and searchlights.",
            ambientSounds = "Distant police sirens, traffic hum, automated transit announcements.",
            connectedLocations = listOf("The Anchor", "Metro Station", "Pawn & Gold", "Murphy's Pub")
        ),
        "Neon Arcade" to LocationInfo(
            id = "neon_arcade",
            district = "Downtown Core",
            name = "Neon Arcade",
            shortDesc = "A flickering arcade packed with vintage CRT cabinets. Behind the curtain lies a chop-shop for electronics.",
            ambientSounds = "Chiptune arcade jingles, solder iron hissing, click of keyboards.",
            connectedLocations = listOf("The Anchor", "Central Plaza", "Pawn & Gold")
        ),
        "Metro Station" to LocationInfo(
            id = "metro_station",
            district = "Downtown Core",
            name = "Metro Station",
            shortDesc = "Tiled walls slick with condensation, rusted turnstiles, and cold platform drafts.",
            ambientSounds = "Screeching steel wheels on tracks, flickering fluorescent hum.",
            connectedLocations = listOf("Central Plaza", "The Anchor", "Tenement Block B")
        ),
        "Pawn & Gold" to LocationInfo(
            id = "pawn_and_gold",
            district = "Downtown Core",
            name = "Pawn & Gold",
            shortDesc = "A narrow shop behind barred windows and bulletproof glass. The counter is lined with forgotten watches and rings.",
            ambientSounds = "Ticking analog clocks, ring of the metal entry bell.",
            connectedLocations = listOf("Central Plaza", "Neon Arcade")
        ),

        // Southside Flats
        "Murphy's Pub" to LocationInfo(
            id = "murphys_pub",
            district = "Southside Flats",
            name = "Murphy's Pub",
            shortDesc = "A worn corner tavern with frosted amber windows, stained mahogany wood, and regulars nursing boilermakers.",
            ambientSounds = "Heavy door creaking, darts thudding in cork, low gruff laughter.",
            connectedLocations = listOf("Low Street Alley", "Tenement Block B", "24/7 Bodega", "Central Plaza")
        ),
        "Tenement Block B" to LocationInfo(
            id = "tenement_block_b",
            district = "Southside Flats",
            name = "Tenement Block B",
            shortDesc = "A crumbling six-story brick residential warren with outdoor mailboxes torn open and narrow hallways.",
            ambientSounds = "Distant crying baby, radiator clanking, footsteps echoing above.",
            connectedLocations = listOf("Murphy's Pub", "Fire Escape", "Low Street Alley")
        ),
        "Low Street Alley" to LocationInfo(
            id = "low_street_alley",
            district = "Southside Flats",
            name = "Low Street Alley",
            shortDesc = "A trash-strewn corridor between damp brick facades with steam vents venting stale grease and rain puddles.",
            ambientSounds = "Rats scurrying through discarded cans, water dripping from rusted gutters.",
            connectedLocations = listOf("Murphy's Pub", "Tenement Block B", "24/7 Bodega", "The Cellar")
        ),
        "24/7 Bodega" to LocationInfo(
            id = "bodega_247",
            district = "Southside Flats",
            name = "24/7 Bodega",
            shortDesc = "Bright buzzing overhead tubes illuminating shelves of instant noodles, energy drinks, and cheap tobacco behind steel wire.",
            ambientSounds = "Refrigeration hum, small television playing muted soccer, chime at the door.",
            connectedLocations = listOf("Murphy's Pub", "Low Street Alley", "Tenement Block B")
        ),
        "Fire Escape" to LocationInfo(
            id = "fire_escape",
            district = "Southside Flats",
            name = "Fire Escape",
            shortDesc = "An iron fire escape clinging to the brick wall, giving a vantage over Southside's alleyways and roofs.",
            ambientSounds = "Gusts of chilly wind, metal stairs vibrating with weight.",
            connectedLocations = listOf("Tenement Block B", "Low Street Alley")
        ),

        // Old Mill District
        "The Cellar" to LocationInfo(
            id = "the_cellar",
            district = "Old Mill District",
            name = "The Cellar",
            shortDesc = "A reinforced basement below an abandoned textile plant. Makeshift boxing ring in the middle, smoke hanging thick.",
            ambientSounds = "Leather gloves smacking canvas, shouting bets, smell of sweat and disinfectant.",
            connectedLocations = listOf("Old Loading Dock", "Rust Ironworks", "Low Street Alley")
        ),
        "Old Loading Dock" to LocationInfo(
            id = "old_loading_dock",
            district = "Old Mill District",
            name = "Old Loading Dock",
            shortDesc = "Weathered wooden pylons and cracked concrete jutting over oily canal waters. Discarded pallets rot in heaps.",
            ambientSounds = "Murky water lapping against algae-coated stone, diesel engine idling in distance.",
            connectedLocations = listOf("The Cellar", "Canal Bridge", "Rust Ironworks")
        ),
        "Rust Ironworks" to LocationInfo(
            id = "rust_ironworks",
            district = "Old Mill District",
            name = "Rust Ironworks",
            shortDesc = "Cavernous corrugated-metal warehouse filled with dormant blast equipment, metal slag, and sparks from illegal cutting torches.",
            ambientSounds = "Grinder squeal, clanking chains, smell of burnt oil.",
            connectedLocations = listOf("The Cellar", "Old Loading Dock", "Abandoned Rail Depot")
        ),
        "Canal Bridge" to LocationInfo(
            id = "canal_bridge",
            district = "Old Mill District",
            name = "Canal Bridge",
            shortDesc = "An arched iron bridge spanning the black waters of the industrial canal. Fog clings heavily to the trusses.",
            ambientSounds = "Wind whistling through steel girders, hollow echo of footsteps.",
            connectedLocations = listOf("Old Loading Dock", "Freight Terminus", "Dead End Motel")
        ),
        "Abandoned Rail Depot" to LocationInfo(
            id = "abandoned_rail_depot",
            district = "Old Mill District",
            name = "Abandoned Rail Depot",
            shortDesc = "Decaying freight cars overgrown with weeds. Makeshift barrel stoves and graffiti mark squatter encampments.",
            ambientSounds = "Woodsmoke cracking, rustling dry grass, distant dog barking.",
            connectedLocations = listOf("Rust Ironworks", "Scrap Yard", "Canal Bridge")
        ),

        // East Yards
        "Yard House" to LocationInfo(
            id = "yard_house",
            district = "East Yards",
            name = "Yard House",
            shortDesc = "A rough roadhouse beside the rail switchboards. Off-shift dockers and Bishop's crew drink hard here.",
            ambientSounds = "Billiard balls cracking, harsh dockworker arguments, jukebox country static.",
            connectedLocations = listOf("Freight Terminus", "Scrap Yard", "Bishop's Club")
        ),
        "Freight Terminus" to LocationInfo(
            id = "freight_terminus",
            district = "East Yards",
            name = "Freight Terminus",
            shortDesc = "Miles of tracks flanked by towering sea-shipping containers under high-powered sodium vapor lamps.",
            ambientSounds = "Hydraulic hissing, heavy freight diesel engines, security siren blips.",
            connectedLocations = listOf("Yard House", "Shipping Containers", "Canal Bridge")
        ),
        "Scrap Yard" to LocationInfo(
            id = "scrap_yard",
            district = "East Yards",
            name = "Scrap Yard",
            shortDesc = "Mountains of crushed automobiles and twisted iron girders behind chain-link fences topped with razor wire.",
            ambientSounds = "Chained rottweilers snarling, metal scraping against metal.",
            connectedLocations = listOf("Yard House", "Abandoned Rail Depot", "Shipping Containers")
        ),
        "Bishop's Club" to LocationInfo(
            id = "bishops_club",
            district = "East Yards",
            name = "Bishop's Club",
            shortDesc = "Discreet mahogany double doors behind a private shipping office. Heavy velvet drapes, cut crystal, and armed guards.",
            ambientSounds = "Muffled smooth jazz, clink of scotch glasses, low calculated tones.",
            connectedLocations = listOf("Yard House", "Shipping Containers")
        ),
        "Shipping Containers" to LocationInfo(
            id = "shipping_containers",
            district = "East Yards",
            name = "Shipping Containers",
            shortDesc = "Narrow labyrinthine alleyways between multi-colored steel freight crates. Many have heavy padlock seals broken.",
            ambientSounds = "Echoing wind, metal reverberating with footsteps.",
            connectedLocations = listOf("Freight Terminus", "Scrap Yard", "Bishop's Club")
        ),

        // Outer Edge
        "Dead End Motel" to LocationInfo(
            id = "dead_end_motel",
            district = "Outer Edge",
            name = "Dead End Motel",
            shortDesc = "Two-story U-shaped motor lodge with peeling turquoise stucco and a buzz of neon that never turns off.",
            ambientSounds = "Neon tube buzzing, car door slamming shut, drone of the nearby highway.",
            connectedLocations = listOf("Highway Overpass", "The Stacks", "Canal Bridge")
        ),
        "Highway Overpass" to LocationInfo(
            id = "highway_overpass",
            district = "Outer Edge",
            name = "Highway Overpass",
            shortDesc = "Massive concrete pillars holding up the interstate viaduct. Shadows shelter oil barrel fires and sleeping bags.",
            ambientSounds = "Rumble of eighteen-wheelers overhead, crackle of burning pallets.",
            connectedLocations = listOf("Dead End Motel", "Trailer Park", "Radio Tower Hill")
        ),
        "The Stacks" to LocationInfo(
            id = "the_stacks",
            district = "Outer Edge",
            name = "The Stacks",
            shortDesc = "A sprawling shantytown built from stacked shipping crates and scavenged sheet metal on the marsh edge.",
            ambientSounds = "Tin roofs rattling in squall, murmurs from crowded dwellings.",
            connectedLocations = listOf("Dead End Motel", "Radio Tower Hill")
        ),
        "Trailer Park" to LocationInfo(
            id = "trailer_park",
            district = "Outer Edge",
            name = "Trailer Park",
            shortDesc = "Potholed gravel road lined with weathered aluminum mobile homes and chain-link dog pens.",
            ambientSounds = "Crickets in the weeds, distant television antenna static.",
            connectedLocations = listOf("Highway Overpass", "Radio Tower Hill")
        ),
        "Radio Tower Hill" to LocationInfo(
            id = "radio_tower_hill",
            district = "Outer Edge",
            name = "Radio Tower Hill",
            shortDesc = "A steep gravel bluff crowned by a red-blinking transmission mast, overlooking the smog and neon grid of Blackridge.",
            ambientSounds = "Howling wind through steel lattice, panoramic view of the whole city below.",
            connectedLocations = listOf("Highway Overpass", "The Stacks", "Trailer Park")
        )
    )

    val INITIAL_NPCS: List<Npc> = listOf(
        Npc(
            id = "darius",
            name = "Darius",
            faction = "Southside Syndicate",
            currentDistrict = "Southside Flats",
            currentLocation = "Murphy's Pub",
            reputation = "Threatening",
            goal = "Shake down deadbeat debtors and expand into the bodega protection circuit.",
            description = "Broad-shouldered loan enforcer in an oilskin coat, scarred cheek, brass rings on heavy knuckles."
        ),
        Npc(
            id = "bishop",
            name = "Bishop",
            faction = "East Yards Cartel",
            currentDistrict = "East Yards",
            currentLocation = "Bishop's Club",
            reputation = "Cold, Wary",
            goal = "Control all freight manifests through the Terminus and purge Southside interlopers.",
            description = "Impeccably tailored older syndicate boss with steel-gray hair and ruthless calculation in his stare."
        ),
        Npc(
            id = "cal",
            name = "Cal",
            faction = "Independent Fence",
            currentDistrict = "Old Mill District",
            currentLocation = "The Cellar",
            reputation = "Businesslike",
            goal = "Move high-value stolen hardware through the loading docks without drawing Bishop's eye.",
            description = "Thin, nervous broker with grease under his fingernails and three different burner phones."
        ),
        Npc(
            id = "rae",
            name = "Rae",
            faction = "Alley Network",
            currentDistrict = "Southside Flats",
            currentLocation = "Low Street Alley",
            reputation = "Volatile",
            goal = "Score a drink or pills, trade dangerous secrets for a safe warm room.",
            description = "Shivering street runner in an oversized trenchcoat, sharp observant eyes beneath matted hair."
        ),
        Npc(
            id = "lena",
            name = "Lena",
            faction = "Data Broker",
            currentDistrict = "Downtown Core",
            currentLocation = "Neon Arcade",
            reputation = "Transactional",
            goal = "Tap into police repeater antennas and sell police dispatch codes.",
            description = "Cool tech specialist wearing heavy headphones and fingerless gloves, surrounded by wiring."
        ),
        Npc(
            id = "sofia",
            name = "Sofia",
            faction = "Outsiders",
            currentDistrict = "Outer Edge",
            currentLocation = "Dead End Motel",
            reputation = "Urgent, Concerned",
            goal = "Find AJ and leave Blackridge before the debt collectors catch up.",
            description = "Tired young woman with a taped travel bag, constantly checking her prepaid phone for reception."
        ),
        Npc(
            id = "rage",
            name = "Rage",
            faction = "Yards Crew",
            currentDistrict = "East Yards",
            currentLocation = "Scrap Yard",
            reputation = "Hostile",
            goal = "Provoke a turf war on Southside borders and prove supremacy to Bishop.",
            description = "Aggressive enforcer with heavy neck tattoos and a spiked steel wrench strapped to his belt."
        ),
        Npc(
            id = "michel",
            name = "Michel",
            faction = "Underground Medic",
            currentDistrict = "Southside Flats",
            currentLocation = "Tenement Block B",
            reputation = "Neutral",
            goal = "Patch wounded street folk quietly without attracting law or syndicate extortion.",
            description = "Disgraced surgeon with steady hands, smelling of rubbing alcohol and cheap menthol cigarettes."
        ),
        Npc(
            id = "murphy",
            name = "Murphy",
            faction = "Civilian",
            currentDistrict = "Southside Flats",
            currentLocation = "Murphy's Pub",
            reputation = "Strict Bartender",
            goal = "Keep the peace in his bar, protect his register, and toss out brawlers.",
            description = "Old pugilist with broken nose, cleaning beer mugs with a stained bar towel."
        )
    )

    // Screen 3 Presets
    fun getPresetState(presetIndex: Int, nsfwSetting: NsfwSetting, apiKey: String): GameState {
        return when (presetIndex) {
            1 -> {
                // Busker Debt
                GameState(
                    turn = 1,
                    day = 1,
                    weekday = Weekday.Friday,
                    timeMinutes = 1230, // 8:30 PM
                    locationDistrict = "Southside Flats",
                    locationName = "Murphy's Pub",
                    cash = 18,
                    hunger = HungerState.Hungry,
                    thirst = ThirstState.Thirsty,
                    health = HealthState.Fine,
                    inventory = listOf(
                        Item("guitar", "Acoustic Guitar", 1, "Scratched acoustic with two replaced nylon strings.", "Gear", 60),
                        Item("picks", "Guitar Picks", 3, "Plastic thumb picks.", "General", 2),
                        Item("coat", "Worn Trench Coat", 1, "Damp canvas coat with deep inside pockets.", "Gear", 15),
                        Item("smokes", "Pack of Smokes", 1, "Six cigarettes left, bent carton.", "Consumable", 5),
                        Item("matches", "Book of Matches", 1, "Murphy's Pub promotional matchbook.", "Tool", 1)
                    ),
                    reputation = mapOf(
                        "Darius (Loan Shark)" to "Threatening ($450 overdue by Sunday)",
                        "Murphy" to "Annoyed by busking near entrance",
                        "Rae" to "Curious, wants a cigarette",
                        "Bishop's Cartel" to "Unaware"
                    ),
                    visitedLocations = setOf("Murphy's Pub"),
                    characterProfile = CharacterProfile(
                        name = "Kaelen",
                        age = 23,
                        sex = "Male",
                        archetype = "Busker",
                        background = "Trapped in Southside Flats after borrowing $400 from Darius for bail.",
                        startingTrait = "Street Performance & Quick Hands"
                    ),
                    nsfwSetting = nsfwSetting,
                    apiKey = apiKey,
                    debtAmount = 450,
                    debtDeadlineMinutes = 2880,
                    promisesAndDebts = listOf("Owes Darius $450 with interest — deadline Sunday midnight"),
                    npcs = INITIAL_NPCS
                )
            }
            2 -> {
                // AJ's First Week
                GameState(
                    turn = 1,
                    day = 1,
                    weekday = Weekday.Tuesday,
                    timeMinutes = 1110, // 6:30 PM
                    locationDistrict = "Downtown Core",
                    locationName = "Metro Station",
                    cash = 40,
                    hunger = HungerState.Full,
                    thirst = ThirstState.Thirsty,
                    health = HealthState.Fine,
                    inventory = listOf(
                        Item("greyhound_stub", "Bus Ticket Stub", 1, "One-way ticket to Blackridge.", "Paper", 0),
                        Item("burner_phone", "Cheap Prepaid Phone", 1, "3 unread voicemails from Sofia.", "Tool", 25),
                        Item("pocket_knife", "Folding Pocket Knife", 1, "3-inch stainless steel blade.", "Weapon", 15),
                        Item("duffle", "Weathered Duffle Bag", 1, "Spare clothes and old family photograph.", "Gear", 20)
                    ),
                    reputation = mapOf(
                        "Sofia" to "Desperately trying to reach you",
                        "Darius" to "Spotted as a fresh mark",
                        "Lena" to "Noticed you exiting the metro gates"
                    ),
                    visitedLocations = setOf("Metro Station"),
                    characterProfile = CharacterProfile(
                        name = "AJ",
                        age = 21,
                        sex = "Male",
                        archetype = "New Arrival",
                        background = "Fled upstate debts; arrived in Blackridge seeking a fresh start.",
                        startingTrait = "Perceptive & Nimble"
                    ),
                    nsfwSetting = nsfwSetting,
                    apiKey = apiKey,
                    debtAmount = 180,
                    debtDeadlineMinutes = 4320,
                    promisesAndDebts = listOf("Sofia is waiting for a callback at Dead End Motel"),
                    npcs = INITIAL_NPCS
                )
            }
            3 -> {
                // Jax's Debt
                GameState(
                    turn = 1,
                    day = 1,
                    weekday = Weekday.Saturday,
                    timeMinutes = 1320, // 10:00 PM
                    locationDistrict = "Old Mill District",
                    locationName = "Old Loading Dock",
                    cash = 12,
                    hunger = HungerState.Starving,
                    thirst = ThirstState.Full,
                    health = HealthState.Minor,
                    inventory = listOf(
                        Item("brass_knuckles", "Heavy Brass Knuckles", 1, "Steel-reinforced knuckle dusters.", "Weapon", 30),
                        Item("medical_tape", "Athletic Tape Roll", 1, "Dirty roll of hand wrapping tape.", "Medical", 5),
                        Item("zippo", "Scratched Chrome Lighter", 1, "Half-full fluid lighter.", "Tool", 10)
                    ),
                    reputation = mapOf(
                        "Cal (Fence)" to "Awaiting fight results / collateral",
                        "Rage's Crew" to "Hunting you after broken jaw incident",
                        "Darius" to "Indifferent unless cash is involved"
                    ),
                    visitedLocations = setOf("Old Loading Dock"),
                    characterProfile = CharacterProfile(
                        name = "Jax",
                        age = 28,
                        sex = "Male",
                        archetype = "Underground Fighter",
                        background = "Bruised knuckles and a bounty on his head in the Mill District.",
                        startingTrait = "Iron Jaw & Street Brawling"
                    ),
                    nsfwSetting = nsfwSetting,
                    apiKey = apiKey,
                    debtAmount = 600,
                    debtDeadlineMinutes = 1440,
                    promisesAndDebts = listOf("Owes underground fight bookie $600 by tomorrow night"),
                    activeStatusEffects = listOf("Bruised Ribs (-1 Mobility)"),
                    npcs = INITIAL_NPCS
                )
            }
            else -> getPresetState(1, nsfwSetting, apiKey)
        }
    }

    fun getExistingCharacterState(name: String, nsfwSetting: NsfwSetting, apiKey: String): GameState {
        return when (name.lowercase()) {
            "riot" -> GameState(
                turn = 1,
                day = 1,
                weekday = Weekday.Wednesday,
                timeMinutes = 1260,
                locationDistrict = "Downtown Core",
                locationName = "Neon Arcade",
                cash = 65,
                hunger = HungerState.Hungry,
                thirst = ThirstState.Full,
                health = HealthState.Fine,
                inventory = listOf(
                    Item("courier_bag", "Reinforced Courier Bag", 1, "Waterproof messenger bag with hidden lining.", "Gear", 45),
                    Item("lockpicks", "Pocket Lockpick Set", 1, "Basic tension wrench and picks.", "Tool", 35),
                    Item("switchblade", "Stiletto Switchblade", 1, "Spring-loaded blade.", "Weapon", 25)
                ),
                reputation = mapOf(
                    "Lena" to "Reliable Courier Partner",
                    "Bishop" to "Watches courier movements with suspicion",
                    "Darius" to "Neutral"
                ),
                visitedLocations = setOf("Neon Arcade"),
                characterProfile = CharacterProfile(
                    name = "Riot",
                    age = 22,
                    sex = "Non-binary",
                    archetype = "Courier",
                    background = "Underground package runner moving sensitive goods across district borders.",
                    startingTrait = "Acrobatic & Roof Runner"
                ),
                nsfwSetting = nsfwSetting,
                apiKey = apiKey,
                npcs = INITIAL_NPCS
            )
            "michel" -> GameState(
                turn = 1,
                day = 1,
                weekday = Weekday.Thursday,
                timeMinutes = 1200,
                locationDistrict = "Southside Flats",
                locationName = "Tenement Block B",
                cash = 90,
                hunger = HungerState.Full,
                thirst = ThirstState.Full,
                health = HealthState.Fine,
                inventory = listOf(
                    Item("medkit", "Surgical Kit", 1, "Scalpel, sutures, antiseptic, gauze.", "Medical", 80),
                    Item("painkillers", "Morphine Vials", 2, "Sealed ampoules.", "Medical", 50),
                    Item("stethoscope", "Stethoscope", 1, "Folded in pocket.", "Tool", 20)
                ),
                reputation = mapOf(
                    "Southside Residents" to "Respected Street Doctor",
                    "Darius" to "Tolerated for medical favors",
                    "Bishop" to "Unacquainted"
                ),
                visitedLocations = setOf("Tenement Block B"),
                characterProfile = CharacterProfile(
                    name = "Michel",
                    age = 38,
                    sex = "Male",
                    archetype = "Medic",
                    background = "Former ER surgeon running an off-grid underground clinic in the Southside flats.",
                    startingTrait = "Surgical Precision & Triage"
                ),
                nsfwSetting = nsfwSetting,
                apiKey = apiKey,
                npcs = INITIAL_NPCS
            )
            "rae" -> GameState(
                turn = 1,
                day = 1,
                weekday = Weekday.Friday,
                timeMinutes = 1245,
                locationDistrict = "Southside Flats",
                locationName = "Low Street Alley",
                cash = 8,
                hunger = HungerState.Starving,
                thirst = ThirstState.Thirsty,
                health = HealthState.Minor,
                inventory = listOf(
                    Item("broken_bottle", "Jagged Bottle", 1, "Improvised glass weapon.", "Weapon", 0),
                    Item("whiskey_flask", "Dent Flask", 1, "Two mouthfuls of harsh rye.", "Consumable", 5),
                    Item("pawn_ticket", "Crinkled Pawn Ticket", 1, "Gold necklace in Pawn & Gold.", "Item", 40)
                ),
                reputation = mapOf(
                    "Murphy" to "Barred from entering tavern",
                    "Darius" to "Annoying debtor",
                    "Cal" to "Occasionally buys intel"
                ),
                visitedLocations = setOf("Low Street Alley"),
                characterProfile = CharacterProfile(
                    name = "Rae",
                    age = 26,
                    sex = "Female",
                    archetype = "Street Survivor",
                    background = "Knows every brick and steam grate in Southside, fighting addiction and cold nights.",
                    startingTrait = "Alley Instincts & Whispers"
                ),
                nsfwSetting = nsfwSetting,
                apiKey = apiKey,
                npcs = INITIAL_NPCS
            )
            else -> getPresetState(1, nsfwSetting, apiKey)
        }
    }
}
