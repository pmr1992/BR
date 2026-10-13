package com.example.blackridge.data

import com.example.blackridge.engine.WorldData
import com.example.blackridge.model.*
import org.json.JSONArray
import org.json.JSONObject

object GameStateSerializer {

    fun serialize(state: GameState): String {
        val root = JSONObject()
        root.put("turn", state.turn)
        root.put("day", state.day)
        root.put("weekday", state.weekday.name)
        root.put("timeMinutes", state.timeMinutes)
        root.put("locationDistrict", state.locationDistrict)
        root.put("locationName", state.locationName)
        root.put("cash", state.cash)
        root.put("hunger", state.hunger.name)
        root.put("thirst", state.thirst.name)
        root.put("health", state.health.name)
        root.put("nsfwSetting", state.nsfwSetting.name)
        root.put("apiKey", state.apiKey)
        root.put("debtAmount", state.debtAmount)
        root.put("debtDeadlineMinutes", state.debtDeadlineMinutes)
        root.put("heatLevel", state.heatLevel)
        root.put("isGameOver", state.isGameOver)
        root.put("gameOverReason", state.gameOverReason)

        // Character profile
        val prof = JSONObject()
        prof.put("name", state.characterProfile.name)
        prof.put("age", state.characterProfile.age)
        prof.put("sex", state.characterProfile.sex)
        prof.put("archetype", state.characterProfile.archetype)
        prof.put("background", state.characterProfile.background)
        prof.put("startingTrait", state.characterProfile.startingTrait)
        root.put("characterProfile", prof)

        // Inventory
        val invArray = JSONArray()
        for (item in state.inventory) {
            val itemObj = JSONObject()
            itemObj.put("id", item.id)
            itemObj.put("name", item.name)
            itemObj.put("quantity", item.quantity)
            itemObj.put("description", item.description)
            itemObj.put("category", item.category)
            itemObj.put("value", item.value)
            invArray.put(itemObj)
        }
        root.put("inventory", invArray)

        // Reputation
        val repObj = JSONObject()
        for ((k, v) in state.reputation) {
            repObj.put(k, v)
        }
        root.put("reputation", repObj)

        // Visited
        val visitedArray = JSONArray()
        for (v in state.visitedLocations) {
            visitedArray.put(v)
        }
        root.put("visitedLocations", visitedArray)

        // Promises
        val promisesArray = JSONArray()
        for (p in state.promisesAndDebts) {
            promisesArray.put(p)
        }
        root.put("promisesAndDebts", promisesArray)

        return root.toString()
    }

    fun deserialize(jsonStr: String): GameState? {
        return try {
            val root = JSONObject(jsonStr)
            val turn = root.optInt("turn", 1)
            val day = root.optInt("day", 1)
            val weekdayName = root.optString("weekday", "Friday")
            val weekday = Weekday.values().find { it.name.equals(weekdayName, ignoreCase = true) } ?: Weekday.Friday
            val timeMinutes = root.optInt("timeMinutes", 1230)
            val locationDistrict = root.optString("locationDistrict", "Southside Flats")
            val locationName = root.optString("locationName", "Murphy's Pub")
            val cash = root.optInt("cash", 18)
            val hunger = HungerState.values().find { it.name.equals(root.optString("hunger"), ignoreCase = true) } ?: HungerState.Hungry
            val thirst = ThirstState.values().find { it.name.equals(root.optString("thirst"), ignoreCase = true) } ?: ThirstState.Thirsty
            val health = HealthState.values().find { it.name.equals(root.optString("health"), ignoreCase = true) } ?: HealthState.Fine
            val nsfw = NsfwSetting.values().find { it.name.equals(root.optString("nsfwSetting"), ignoreCase = true) } ?: NsfwSetting.OFF
            val apiKey = root.optString("apiKey", "")
            val debtAmount = root.optInt("debtAmount", 450)
            val debtDeadlineMinutes = root.optInt("debtDeadlineMinutes", 2880)
            val heatLevel = root.optInt("heatLevel", 1)
            val isGameOver = root.optBoolean("isGameOver", false)
            val gameOverReason = root.optString("gameOverReason", "")

            val profObj = root.optJSONObject("characterProfile")
            val profile = if (profObj != null) {
                CharacterProfile(
                    name = profObj.optString("name", "Kaelen"),
                    age = profObj.optInt("age", 24),
                    sex = profObj.optString("sex", "Male"),
                    archetype = profObj.optString("archetype", "Busker"),
                    background = profObj.optString("background", ""),
                    startingTrait = profObj.optString("startingTrait", "")
                )
            } else CharacterProfile("Kaelen", 24, "Male")

            val invList = mutableListOf<Item>()
            val invArray = root.optJSONArray("inventory")
            if (invArray != null) {
                for (i in 0 until invArray.length()) {
                    val itemObj = invArray.getJSONObject(i)
                    invList.add(
                        Item(
                            id = itemObj.optString("id"),
                            name = itemObj.optString("name"),
                            quantity = itemObj.optInt("quantity", 1),
                            description = itemObj.optString("description", ""),
                            category = itemObj.optString("category", "General"),
                            value = itemObj.optInt("value", 0)
                        )
                    )
                }
            }

            val repMap = mutableMapOf<String, String>()
            val repObj = root.optJSONObject("reputation")
            if (repObj != null) {
                val keys = repObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    repMap[k] = repObj.optString(k)
                }
            }

            val visitedSet = mutableSetOf<String>()
            val visitedArray = root.optJSONArray("visitedLocations")
            if (visitedArray != null) {
                for (i in 0 until visitedArray.length()) {
                    visitedSet.add(visitedArray.getString(i))
                }
            }

            val promisesList = mutableListOf<String>()
            val promisesArray = root.optJSONArray("promisesAndDebts")
            if (promisesArray != null) {
                for (i in 0 until promisesArray.length()) {
                    promisesList.add(promisesArray.getString(i))
                }
            }

            GameState(
                turn = turn,
                day = day,
                weekday = weekday,
                timeMinutes = timeMinutes,
                locationDistrict = locationDistrict,
                locationName = locationName,
                cash = cash,
                hunger = hunger,
                thirst = thirst,
                health = health,
                inventory = invList,
                reputation = repMap,
                visitedLocations = visitedSet,
                characterProfile = profile,
                nsfwSetting = nsfw,
                apiKey = apiKey,
                debtAmount = debtAmount,
                debtDeadlineMinutes = debtDeadlineMinutes,
                heatLevel = heatLevel,
                promisesAndDebts = promisesList,
                npcs = WorldData.INITIAL_NPCS,
                isGameOver = isGameOver,
                gameOverReason = gameOverReason
            )
        } catch (_: Exception) {
            null
        }
    }
}
