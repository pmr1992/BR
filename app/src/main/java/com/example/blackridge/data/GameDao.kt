package com.example.blackridge.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM game_saves WHERE slotId = :slotId")
    suspend fun getSaveBySlot(slotId: Int): GameSaveEntity?

    @Query("SELECT * FROM game_saves ORDER BY lastUpdated DESC")
    fun getAllSavesFlow(): Flow<List<GameSaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSave(save: GameSaveEntity)

    @Query("DELETE FROM game_saves WHERE slotId = :slotId")
    suspend fun deleteSave(slotId: Int)

    // Turn Logs
    @Insert
    suspend fun insertTurnLog(turnLog: TurnLogEntity)

    @Query("SELECT * FROM turn_logs WHERE slotId = :slotId ORDER BY turnNumber ASC")
    suspend fun getTurnLogsForSlot(slotId: Int): List<TurnLogEntity>

    @Query("SELECT * FROM turn_logs WHERE slotId = :slotId ORDER BY turnNumber DESC LIMIT 50")
    fun getRecentTurnLogsFlow(slotId: Int): Flow<List<TurnLogEntity>>

    @Query("DELETE FROM turn_logs WHERE slotId = :slotId")
    suspend fun deleteLogsForSlot(slotId: Int)
}
