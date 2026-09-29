package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FilterLogEntity): Long

    @Query("SELECT * FROM filter_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<FilterLogEntity>>

    @Query("SELECT COUNT(*) FROM filter_logs WHERE isBlocked = 1")
    fun getBlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM filter_logs WHERE isBlocked = 0")
    fun getAllowedCount(): Flow<Int>

    @Query("DELETE FROM filter_logs WHERE id NOT IN (SELECT id FROM filter_logs ORDER BY timestamp DESC LIMIT 200)")
    suspend fun trimOldLogs()

    @Query("DELETE FROM filter_logs")
    suspend fun clearAllLogs()
}
