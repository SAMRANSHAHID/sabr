package com.example.data.repository

import android.content.Context
import com.example.data.local.CustomRuleEntity
import com.example.data.local.FilterLogEntity
import com.example.domain.model.Category
import com.example.domain.model.FilterDecision
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.flow.Flow

interface ProtectionRepository {
    val sessionState: Flow<UnblockSessionState>
    val hasSeenOnboarding: Flow<Boolean>
    val safeSearchEnabled: Flow<Boolean>
    val blockGambling: Flow<Boolean>
    val blockDating: Flow<Boolean>
    val recentLogs: Flow<List<FilterLogEntity>>
    val blockedCount: Flow<Int>
    val allowedCount: Flow<Int>
    val customRules: Flow<List<CustomRuleEntity>>

    suspend fun enableProtection(context: Context): Boolean
    suspend fun requestUnblock(): UnblockSessionState
    suspend fun startHour(): UnblockSessionState
    suspend fun cancelUnblock(): UnblockSessionState
    suspend fun finalizeUnblock(context: Context): Boolean
    suspend fun syncTimerState(now: Long = System.currentTimeMillis()): UnblockSessionState

    suspend fun setHasSeenOnboarding(hasSeen: Boolean)
    suspend fun setSafeSearchEnabled(enabled: Boolean)
    suspend fun setBlockGambling(enabled: Boolean)
    suspend fun setBlockDating(enabled: Boolean)
    suspend fun setStageDurationForTesting(durationMs: Long)

    suspend fun addCustomRule(domain: String, isAllowed: Boolean, category: Category, note: String = "")
    suspend fun removeCustomRule(id: Long)
    suspend fun removeCustomRuleByDomain(domain: String)

    suspend fun logFilterEvent(domain: String, isBlocked: Boolean, category: String)
    suspend fun clearLogs()

    fun testDomainClassification(domain: String): FilterDecision
}
