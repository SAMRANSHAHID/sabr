package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRuleDao {
    @Query("SELECT * FROM custom_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<CustomRuleEntity>>

    @Query("SELECT * FROM custom_rules WHERE isAllowed = 1")
    fun getAllowedRules(): Flow<List<CustomRuleEntity>>

    @Query("SELECT * FROM custom_rules WHERE isAllowed = 0")
    fun getBlockedRules(): Flow<List<CustomRuleEntity>>

    @Query("SELECT * FROM custom_rules WHERE domain = :domain LIMIT 1")
    suspend fun findRuleByDomain(domain: String): CustomRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: CustomRuleEntity): Long

    @Query("DELETE FROM custom_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("DELETE FROM custom_rules WHERE domain = :domain")
    suspend fun deleteRuleByDomain(domain: String)

    @Query("SELECT * FROM custom_rules")
    suspend fun getAllRulesDirect(): List<CustomRuleEntity>
}
