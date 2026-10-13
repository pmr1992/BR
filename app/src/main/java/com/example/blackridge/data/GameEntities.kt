package com.example.blackridge.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_saves")
data class GameSaveEntity(
    @PrimaryKey
    val slotId: Int = 1,
    val characterName: String,
    val archetype: String,
    val turn: Int,
    val day: Int,
    val weekday: String,
    val timeFormatted: String,
    val locationName: String,
    val district: String,
    val cash: Int,
    val hunger: String,
    val thirst: String,
    val health: String,
    val nsfwSetting: String,
    val apiKey: String,
    val stateJson: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "turn_logs")
data class TurnLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val slotId: Int = 1,
    val turnNumber: Int,
    val playerAction: String,
    val turnSkeletonText: String,
    val timestamp: Long = System.currentTimeMillis()
)
