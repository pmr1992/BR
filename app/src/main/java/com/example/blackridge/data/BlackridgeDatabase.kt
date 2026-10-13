package com.example.blackridge.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameSaveEntity::class, TurnLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BlackridgeDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: BlackridgeDatabase? = null

        fun getDatabase(context: Context): BlackridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlackridgeDatabase::class.java,
                    "blackridge_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
