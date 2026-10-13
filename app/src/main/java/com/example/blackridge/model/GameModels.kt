package com.example.blackridge.model

enum class NsfwSetting {
    OFF,
    LIGHT,
    FULL
}

enum class HealthState(val displayName: String, val severityLevel: Int) {
    Fine("Fine", 0),
    Minor("Minor", 1),
    Moderate("Moderate", 2),
    Serious("Serious", 3),
    Critical("Critical", 4)
}

enum class HungerState(val displayName: String) {
    Full("Full"),
    Hungry("Hungry"),
    Starving("Starving")
}

enum class ThirstState(val displayName: String) {
    Full("Full"),
    Thirsty("Thirsty"),
    Dehydrated("Dehydrated")
}

enum class Weekday(val displayName: String) {
    Monday("Monday"),
    Tuesday("Tuesday"),
    Wednesday("Wednesday"),
    Thursday("Thursday"),
    Friday("Friday"),
    Saturday("Saturday"),
    Sunday("Sunday")
}

data class Item(
    val id: String,
    val name: String,
    val quantity: Int = 1,
    val description: String = "",
    val category: String = "General",
    val value: Int = 0
)

data class CharacterProfile(
    val name: String,
    val age: Int,
    val sex: String,
    val archetype: String = "Drifter",
    val background: String = "Arrived with empty pockets and debts.",
    val startingTrait: String = "Street Smarts"
)

data class Npc(
    val id: String,
    val name: String,
    val faction: String,
    val currentDistrict: String,
    val currentLocation: String,
    val reputation: String,
    val goal: String,
    val status: String = "Active",
    val description: String = ""
)

data class LocationInfo(
    val id: String,
    val district: String,
    val name: String,
    val shortDesc: String,
    val ambientSounds: String,
    val connectedLocations: List<String>
)

data class TurnOutput(
    val turnNumber: Int,
    val dayNumber: Int,
    val weekday: String,
    val timeFormatted: String,
    val location: String,
    val cash: Int,
    val hunger: String,
    val thirst: String,
    val inventoryText: String,
    val health: String,
    val crewRepText: String,
    val scene: String,
    val worldTick: String,
    val npcAction: String,
    val stateChanges: String,
    val pic: String,
    val actionA: String,
    val actionB: String,
    val actionC: String,
    val actionD: String = "freeform — type anything",
    val isEntryRule: Boolean = false,
    val rawFormattedText: String = "",
    val playerAction: String = ""
) {
    fun toSkeletonString(): String {
        return buildString {
            appendLine("=== BLACKRIDGE ===")
            appendLine("TURN: $turnNumber        DAY $dayNumber — $weekday — $timeFormatted")
            appendLine("LOCATION: $location")
            appendLine("CASH: $$cash   HUNGER: $hunger   THIRST: $thirst")
            appendLine("INVENTORY: $inventoryText         HEALTH: $health")
            appendLine("CREW/REP: $crewRepText")
            appendLine()
            appendLine("--- SCENE ---")
            appendLine(scene)
            appendLine()
            appendLine("--- WORLD TICK ---")
            appendLine(worldTick)
            appendLine()
            appendLine("--- NPC ACTION ---")
            appendLine(npcAction)
            appendLine()
            appendLine("--- STATE CHANGES ---")
            appendLine(stateChanges)
            appendLine()
            appendLine("--- PIC ---")
            appendLine(pic)
            appendLine()
            appendLine("ACTIONS:")
            if (isEntryRule) {
                appendLine("[FIRST VISIT ENTRY RULE: Type freeform action to enter or scope out the area]")
                appendLine("A) Walk inside and observe the room")
                appendLine("B) Keep your head down and look for familiar faces")
                appendLine("C) Scope the exits and check who is watching the door")
                appendLine("D) $actionD")
            } else {
                appendLine("A) $actionA")
                appendLine("B) $actionB")
                appendLine("C) $actionC")
                appendLine("D) $actionD")
            }
        }
    }
}
