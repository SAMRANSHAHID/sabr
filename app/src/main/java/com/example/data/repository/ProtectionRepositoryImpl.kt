package com.example.data.repository

import android.content.Context
import com.example.core.filter.AllowlistManager
import com.example.core.filter.BlocklistManager
import com.example.core.filter.DnsFilter
import com.example.core.filter.DomainClassifier
import com.example.data.local.CustomRuleDao
import com.example.data.local.CustomRuleEntity
import com.example.data.local.DataStoreManager
import com.example.data.local.FilterLogDao
import com.example.data.local.FilterLogEntity
import com.example.domain.manager.ProtectionStateManager
import com.example.domain.model.Category
import com.example.domain.model.FilterDecision
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProtectionRepositoryImpl(
    private val protectionStateManager: ProtectionStateManager,
    private val dataStoreManager: DataStoreManager,
    private val customRuleDao: CustomRuleDao,
    private val filterLogDao: FilterLogDao,
    private val domainClassifier: DomainClassifier,
    private val dnsFilter: DnsFilter,
    private val blocklistManager: BlocklistManager,
    private val allowlistManager: AllowlistManager,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : ProtectionRepository {

    override val sessionState: Flow<UnblockSessionState> = protectionStateManager.stateFlow
    override val hasSeenOnboarding: Flow<Boolean> = dataStoreManager.hasSeenOnboardingFlow
    override val safeSearchEnabled: Flow<Boolean> = dataStoreManager.safeSearchEnabledFlow
    override val blockGambling: Flow<Boolean> = dataStoreManager.blockGamblingFlow
    override val blockDating: Flow<Boolean> = dataStoreManager.blockDatingFlow
    override val recentLogs: Flow<List<FilterLogEntity>> = filterLogDao.getRecentLogs(50)
    override val blockedCount: Flow<Int> = filterLogDao.getBlockedCount()
    override val allowedCount: Flow<Int> = filterLogDao.getAllowedCount()
    override val customRules: Flow<List<CustomRuleEntity>> = customRuleDao.getAllRules()

    init {
        // Sync preferences with engine managers
        externalScope.launch {
            dataStoreManager.safeSearchEnabledFlow.collectLatest { enabled ->
                dnsFilter.setSafeSearchEnabled(enabled)
            }
        }
        externalScope.launch {
            dataStoreManager.blockGamblingFlow.collectLatest { enabled ->
                blocklistManager.setGamblingBlockingEnabled(enabled)
            }
        }
        externalScope.launch {
            dataStoreManager.blockDatingFlow.collectLatest { enabled ->
                blocklistManager.setDatingBlockingEnabled(enabled)
            }
        }
    }

    override suspend fun enableProtection(context: Context): Boolean {
        return protectionStateManager.enableProtection(context)
    }

    override suspend fun requestUnblock(): UnblockSessionState {
        return protectionStateManager.requestUnblock()
    }

    override suspend fun startHour(): UnblockSessionState {
        return protectionStateManager.startHour()
    }

    override suspend fun cancelUnblock(): UnblockSessionState {
        return protectionStateManager.cancelUnblock()
    }

    override suspend fun finalizeUnblock(context: Context): Boolean {
        return protectionStateManager.finalizeUnblock(context)
    }

    override suspend fun syncTimerState(now: Long): UnblockSessionState {
        return protectionStateManager.syncState(now)
    }

    override suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        dataStoreManager.setHasSeenOnboarding(hasSeen)
    }

    override suspend fun setSafeSearchEnabled(enabled: Boolean) {
        dataStoreManager.setSafeSearchEnabled(enabled)
    }

    override suspend fun setBlockGambling(enabled: Boolean) {
        dataStoreManager.setBlockGambling(enabled)
    }

    override suspend fun setBlockDating(enabled: Boolean) {
        dataStoreManager.setBlockDating(enabled)
    }

    override suspend fun setStageDurationForTesting(durationMs: Long) {
        dataStoreManager.setStageDurationForTesting(durationMs)
    }

    override suspend fun addCustomRule(
        domain: String,
        isAllowed: Boolean,
        category: Category,
        note: String
    ) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").removeSuffix("/")
        if (clean.isEmpty()) return

        customRuleDao.insertRule(
            CustomRuleEntity(
                domain = clean,
                isAllowed = isAllowed,
                category = category.name,
                note = note
            )
        )
        if (isAllowed) {
            allowlistManager.addDomain(clean)
        } else {
            blocklistManager.addDomain(clean, category)
        }
    }

    override suspend fun removeCustomRule(id: Long) {
        customRuleDao.deleteRuleById(id)
    }

    override suspend fun removeCustomRuleByDomain(domain: String) {
        val clean = domain.trim().lowercase()
        customRuleDao.deleteRuleByDomain(clean)
        allowlistManager.removeDomain(clean)
        blocklistManager.removeDomain(clean)
    }

    override suspend fun logFilterEvent(domain: String, isBlocked: Boolean, category: String) {
        filterLogDao.insertLog(
            FilterLogEntity(
                domain = domain,
                isBlocked = isBlocked,
                category = category
            )
        )
        filterLogDao.trimOldLogs()
    }

    override suspend fun clearLogs() {
        filterLogDao.clearAllLogs()
    }

    override fun testDomainClassification(domain: String): FilterDecision {
        return domainClassifier.classify(domain)
    }
}
