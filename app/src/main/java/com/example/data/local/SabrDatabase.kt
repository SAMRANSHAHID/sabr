package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CustomRuleEntity::class, FilterLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SabrDatabase : RoomDatabase() {
    abstract fun customRuleDao(): CustomRuleDao
    abstract fun filterLogDao(): FilterLogDao

    companion object {
        @Volatile
        private var INSTANCE: SabrDatabase? = null

        fun getInstance(context: Context): SabrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SabrDatabase::class.java,
                    "sabr_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
