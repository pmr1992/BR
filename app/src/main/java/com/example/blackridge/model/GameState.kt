package com.example.blackridge.model

data class GameState(
    val turn: Int = 1,
    val day: Int = 1,
    val weekday: Weekday = Weekday.Friday,
    val timeMinutes: Int = 1230, // 8:30 PM (20 * 60 + 30)
    val locationDistrict: String = "Southside Flats",
    val locationName: String = "Murphy's Pub",
    val cash: Int = 18,
    val hunger: HungerState = HungerState.Hungry,
    val thirst: ThirstState = ThirstState.Thirsty,
    val health: HealthState = HealthState.Fine,
    val inventory: List<Item> = emptyList(),
    val reputation: Map<String, String> = emptyMap(),
    val visitedLocations: Set<String> = emptySet(),
    val characterProfile: CharacterProfile = CharacterProfile(
        name = "Kaelen",
        age = 24,
        sex = "Male",
        archetype = "Busker",
        background = "Busking on low street, trapped by loan shark interest.",
        startingTrait = "Quick Hands"
    ),
    val nsfwSetting: NsfwSetting = NsfwSetting.OFF,
    val apiKey: String = "",
    val debtAmount: Int = 450,
    val debtDeadlineMinutes: Int = 2880, // 48 hours
    val heatLevel: Int = 1, // 1-5
    val promisesAndDebts: List<String> = listOf("Owes Darius $450 by Sunday midnight"),
    val activeStatusEffects: List<String> = emptyList(),
    val npcs: List<Npc> = emptyList(),
    val isGameOver: Boolean = false,
    val gameOverReason: String = ""
) {
    fun formatTime(): String {
        val totalMinutes = timeMinutes % (24 * 60)
        val hour24 = totalMinutes / 60
        val minute = totalMinutes % 60
        val isPm = hour24 >= 12
        val hour12 = when (hour24 % 12) {
            0 -> 12
            else -> hour24 % 12
        }
        val amPm = if (isPm) "PM" else "AM"
        return String.format("%02d:%02d %s", hour12, minute, amPm)
    }

    fun getFullLocationString(): String = "$locationName ($locationDistrict)"

    fun getInventorySummary(): String {
        if (inventory.isEmpty()) return "None"
        return inventory.joinToString(", ") {
            if (it.quantity > 1) "${it.name} x${it.quantity}" else it.name
        }
    }

    fun getCrewRepSummary(): String {
        if (reputation.isEmpty()) return "Neutral, unknown face in the crowd."
        return reputation.entries.take(3).joinToString("; ") { "${it.key}: ${it.value}" }
    }
}
